// safety.js
/* global SignaturePad */

/** @type {SignaturePad|null} 서명 패드 인스턴스 */
let signaturePadInstance = null;
/** @type {File|null} 서명 이미지 파일 객체 */
let signatureFile = null;
/** 현재 수정 중인 글의 ID (null이면 새 글 작성) */
let currentEditingId = null;

const checklistData = {
    electric: ["작업 전 전원 차단 및 검전 실시", "절연 장갑 등 보호구 착용", "누전 차단기 작동 상태 확인", "전선 피복 손상 여부"],
    partition: ["자재 절단 시 보안경 착용", "작업 발판(우마) 전도 방지", "자재 적재 시 무너짐 방지", "타카 사용 시 오발 주의"],
    scaffold: ["비계 기둥/띠장 체결 확인", "작업 발판 고정 및 틈새 확인", "추락 방지망/난간대 설치", "적재 하중 준수"],
    plumbing: ["화기 감시자 배치 확인", "밀폐 공간 산소 농도 측정", "절단면 베임 주의", "중량물 운반 자세 준수"],
    duct: ["고소 작업 안전대 착용", "절단면 손 베임 주의", "천장 앵커 고정 상태", "TL 아웃트리거 설치"],
    steel: ["안전대 부착 설비(생명줄) 확인", "인양 와이어/샤클 체결 점검", "볼트 체결 시 낙하물 방지", "강풍 시 작업 중지"],
    form: ["동바리 수직/수평 고정", "자재 인양 하부 통제", "찔림 사고 방지 조치", "콘크리트 타설 압력 확인"],
    welding: ["소화기/방지포 비치", "용접 흄 환기 상태", "용접기 외함 접지", "화상 방지 보호구 착용"]
};

// =================================================================================
//                                2. 초기화 및 이벤트 리스너
// =================================================================================

document.addEventListener('DOMContentLoaded', () => {
    autoFillDateAndWeather();

    const urlParams = new URLSearchParams(window.location.search);
    const logId = urlParams.get('id');

    if (logId) {
        loadLogDataForEdit(logId).catch(err => console.error("데이터 로딩 실패:", err));
    } else {
        autoCheckLastLog().catch(err => console.error("전일 데이터 확인 실패:", err));
    }

    const workTypeSelect = document.getElementById('workTypeSelect');
    const workTypeDirect = document.getElementById('workTypeDirect');

    if (workTypeSelect instanceof HTMLSelectElement && workTypeDirect instanceof HTMLInputElement) {
        workTypeSelect.addEventListener('change', function() {
            updateChecklist();
            // @ts-ignore
            if (this.value === 'direct') {
                workTypeDirect.style.display = 'block';
                workTypeDirect.focus();
            } else {
                workTypeDirect.style.display = 'none';
                workTypeDirect.value = '';
            }
        });
    }

    initSignaturePad();

    const submitBtn = document.getElementById('finalSubmitBtn');
    if (submitBtn) submitBtn.addEventListener('click', saveLog);

    const aiBtn = document.getElementById('btn-analyze-ai');
    if (aiBtn) {
        aiBtn.addEventListener('click', () => {
            runSafetyAiAnalysis().catch(err => alert("AI 분석 중 오류가 발생했습니다: " + err.message));
        });
    }

    const pdfBtn = document.getElementById('btn-download-pdf');
    if (pdfBtn && logId) {
        pdfBtn.style.display = 'inline-block';
        pdfBtn.addEventListener('click', () => {
            window.location.href = `/api/safety-log/${logId}/pdf`;
        });
    }
});

// =================================================================================
//                                3. 데이터 로딩 및 폼 채우기
// =================================================================================

async function autoCheckLastLog() {
    try {
        const response = await fetch('/api/safety-log/last');
        if (response.status === 204 || !response.ok) return;

        if (confirm("📢 알림\n가장 최근에 작성한 안전일지 내용이 있습니다.\n불러오시겠습니까?")) {
            const dto = await response.json();
            fillFormWithData(dto);
        }
    } catch (e) {
        console.error(e);
    }
}

async function loadLogDataForEdit(id) {
    try {
        const response = await fetch(`/api/safety-log/${id}`);
        if (!response.ok) throw new Error("조회 실패");

        const dto = await response.json();
        currentEditingId = id;

        const btn = document.getElementById('finalSubmitBtn');
        if (btn) btn.textContent = "수정 내용 저장";

        fillFormWithData(dto);

        const dateEl = document.getElementById('inspectionDate');
        if (dateEl instanceof HTMLInputElement) dateEl.value = dto.logDate;
    } catch (e) {
        alert("로딩 실패: " + e.message);
    }
}

function fillFormWithData(dto) {
    const fields = ['company', 'location', 'author', 'manager', 'workDetails', 'remarks', 'potentialRiskFactors', 'countermeasures', 'majorRiskFactors'];
    fields.forEach(id => {
        const el = document.getElementById(id);
        if (el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement) {
            el.value = dto[id] || "";
        }
    });

    const wCount = document.getElementById('workersCount');
    if (wCount instanceof HTMLInputElement) wCount.value = dto.workersCount || 0;

    const wNames = document.getElementById('workerNames');
    if (wNames instanceof HTMLInputElement) wNames.value = (dto.workerNames || []).join(', ');

    // 공종 선택 로직
    const select = document.getElementById('workTypeSelect');
    const directInput = document.getElementById('workTypeDirect');

    if (select instanceof HTMLSelectElement && directInput instanceof HTMLInputElement) {
        let optionExists = false;
        for (let i = 0; i < select.options.length; i++) {
            if (select.options[i].value === dto.workType) {
                optionExists = true;
                break;
            }
        }

        if (optionExists) {
            select.value = dto.workType;
            directInput.style.display = 'none';
        } else {
            select.value = 'direct';
            directInput.value = dto.workType;
            directInput.style.display = 'block';
        }
    }

    updateChecklist();
    if (dto.safetyChecklist) {
        const savedItems = dto.safetyChecklist.map(obj => obj.item);
        document.querySelectorAll('.safety-item').forEach(box => {
            if (box instanceof HTMLInputElement) {
                box.checked = savedItems.includes(box.value);
            }
        });
    }
}

function autoFillDateAndWeather() {
    const dateInput = document.getElementById('inspectionDate');
    if (dateInput instanceof HTMLInputElement && !dateInput.value) {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');
        dateInput.value = `${year}-${month}-${day}`;
    }

    const weatherInput = document.getElementById('weather');
    if (weatherInput instanceof HTMLInputElement && !weatherInput.value && navigator.geolocation) {
        weatherInput.placeholder = "날씨 불러오는 중...";

        navigator.geolocation.getCurrentPosition(async (position) => {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;
            try {
                const url = `/api/weather?lat=${lat}&lon=${lon}`;
                const response = await fetch(url);
                if (!response.ok) throw new Error("통신 실패");
                const data = await response.json();
                weatherInput.value = data.info;
            } catch (e) {
                weatherInput.placeholder = "직접 입력하세요";
            }
        }, () => {
            weatherInput.placeholder = "위치 권한 필요";
        });
    }
}

function updateChecklist() {
    const workTypeSelect = document.getElementById("workTypeSelect");
    const container = document.getElementById("checklistContainer");

    if (!container || !(workTypeSelect instanceof HTMLSelectElement)) return;

    const val = workTypeSelect.value;
    container.innerHTML = "";

    if (!val || val === 'direct' || !checklistData[val]) {
        container.innerHTML = '<p style="color: #888; margin: 0; text-align: center; padding: 20px;">공종을 선택하면 점검 항목이 나타납니다.</p>';
        return;
    }

    checklistData[val].forEach((item) => {
        const div = document.createElement("div");
        div.className = 'checklist-item';
        div.innerHTML = `<label><input type="checkbox" class="safety-item" value="${item}" checked> ${item}</label>`;
        container.appendChild(div);
    });
}

// @ts-ignore (HTML onclick에서 호출됨)
window.addManualItem = function() {
    const input = document.getElementById('manualCheckInput');
    const container = document.getElementById('checklistContainer');

    if (!(input instanceof HTMLInputElement) || !container) return;

    const text = input.value.trim();
    if (!text) {
        alert("추가할 점검 내용을 입력해주세요.");
        return;
    }

    if (container.querySelector('p')) {
        container.innerHTML = '';
    }

    const div = document.createElement("div");
    div.className = 'checklist-item';
    div.innerHTML = `
        <label style="flex: 1; display: flex; align-items: center;">
            <input type="checkbox" class="safety-item" value="${text}" checked> 
            <span style="margin-left: 8px;">${text}</span>
        </label>
        <button type="button" onclick="this.parentElement.remove()" 
                style="color: #e74c3c; background: none; border: none; cursor: pointer; font-size: 0.8rem; margin-left: 10px;">
            [삭제]
        </button>
    `;

    container.insertBefore(div, container.firstChild);
    input.value = '';
    input.focus();
};

// =================================================================================
//                                5. AI 및 서명 기능
// =================================================================================

async function runSafetyAiAnalysis() {
    const fileInput = document.getElementById('photosInput');
    const riskArea = document.getElementById('potentialRiskFactors');
    const measureArea = document.getElementById('countermeasures');
    const btn = document.getElementById('btn-analyze-ai');

    if (!(fileInput instanceof HTMLInputElement) || !fileInput.files || fileInput.files.length === 0) {
        throw new Error("사진을 업로드해주세요.");
    }
    if (!(riskArea instanceof HTMLTextAreaElement) || !(measureArea instanceof HTMLTextAreaElement) || !btn) return;

    const originalText = btn.textContent;
    btn.disabled = true;
    btn.textContent = "분석 중...";
    riskArea.placeholder = "AI가 위험 요소를 분석 중입니다...";
    measureArea.placeholder = "대책을 수립 중입니다...";

    try {
        const formData = new FormData();
        formData.append("file", fileInput.files[0]);

        const response = await fetch('/api/safety-log/analyze-photo', { method: 'POST', body: formData });

        if (response.ok) {
            const data = await response.json();
            const fullText = data.description;
            if (fullText.includes("///")) {
                const parts = fullText.split("///");
                riskArea.value = parts[0].replace(/위험요인:|위험 요인:/g, "").trim();
                measureArea.value = parts[1].replace(/대책:|안전 대책:/g, "").trim();
            } else {
                riskArea.value = fullText;
                measureArea.value = "AI 응답 형식을 자동으로 분리하지 못했습니다. 위험요인 칸을 확인하세요.";
            }
        } else {
            throw new Error("서버 오류");
        }
    } finally {
        btn.disabled = false;
        btn.textContent = originalText;
    }
}

function initSignaturePad() {
    const canvas = document.getElementById('signature-pad');
    if (!(canvas instanceof HTMLCanvasElement)) return;

    signaturePadInstance = new SignaturePad(canvas, { backgroundColor: 'rgb(255, 255, 255)' });

    function resizeCanvas() {
        const ratio = Math.max(window.devicePixelRatio || 1, 1);
        // @ts-ignore
        canvas.width = canvas.offsetWidth * ratio;
        // @ts-ignore
        canvas.height = canvas.offsetHeight * ratio;
        canvas.getContext("2d").scale(ratio, ratio);
        if (signaturePadInstance) signaturePadInstance.clear();
    }
    window.addEventListener("resize", resizeCanvas);
    resizeCanvas();

    const clearBtn = document.getElementById('sig-clear-btn');
    if (clearBtn) clearBtn.addEventListener('click', () => {
        if (signaturePadInstance) signaturePadInstance.clear();
    });

    const saveBtn = document.getElementById('sig-save-btn');
    if (saveBtn) saveBtn.addEventListener('click', () => {
        if (!signaturePadInstance || signaturePadInstance.isEmpty()) return alert("서명해 주세요.");
        const dataURL = signaturePadInstance.toDataURL("image/png");
        const arr = dataURL.split(',');
        const mime = arr[0].match(/:(.*?);/)[1];
        const binaryString = atob(arr[1]);
        let n = binaryString.length;
        const u8arr = new Uint8Array(n);
        while(n--) { u8arr[n] = binaryString.charCodeAt(n); }
        signatureFile = new File([u8arr], "signature.png", { type: mime });
        alert("서명이 임시 저장되었습니다.");
    });
}

// =================================================================================
//                                6. 저장 로직
// =================================================================================

async function uploadFiles(inputId) {
    const inputElement = document.getElementById(inputId);
    if (!(inputElement instanceof HTMLInputElement) || !inputElement.files) return [];
    const files = inputElement.files;
    const paths = [];
    for (const file of files) {
        paths.push(await uploadSingleFile(file));
    }
    return paths;
}

async function uploadSingleFile(file) {
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch('/api/files/upload', { method: 'POST', body: formData });
    if (!res.ok) throw new Error(`업로드 실패 (${res.status})`);
    const data = await res.json();
    return data.filePath;
}

async function saveLog() {
    let workType = '';
    const wSelect = document.getElementById('workTypeSelect');
    const wDirect = document.getElementById('workTypeDirect');

    if (wSelect instanceof HTMLSelectElement) workType = wSelect.value;
    if (workType === 'direct' && wDirect instanceof HTMLInputElement) {
        workType = wDirect.value;
    }

    const submitBtn = document.getElementById('finalSubmitBtn');
    if (submitBtn instanceof HTMLButtonElement) {
        submitBtn.disabled = true;
        submitBtn.textContent = '저장 중...';
    }

    try {
        const uploadedPhotos = await uploadFiles('photosInput');
        const uploadedAttachments = await uploadFiles('attachmentsInput');

        let followUpPhotoPath = "";
        const followUpInput = document.getElementById('followUpPhotoInput');
        if (followUpInput instanceof HTMLInputElement && followUpInput.files.length > 0) {
            followUpPhotoPath = await uploadSingleFile(followUpInput.files[0]);
        }

        let signaturePath = "";
        const sigInput = document.getElementById('signatureInput');
        if (sigInput instanceof HTMLInputElement && sigInput.files.length > 0) {
            signaturePath = await uploadSingleFile(sigInput.files[0]);
        } else if (signatureFile) {
            signaturePath = await uploadSingleFile(signatureFile);
        }

        const checklistItems = [];
        document.querySelectorAll('.safety-item').forEach(box => {
            if (box instanceof HTMLInputElement && box.checked) {
                checklistItems.push({ item: box.value, status: "양호" });
            }
        });

        const getValue = (id) => {
            const el = document.getElementById(id);
            if (el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement) return el.value;
            return "";
        };

        const logData = {
            company: getValue('company'),
            logDate: getValue('inspectionDate'),
            weather: getValue('weather'),
            location: getValue('location'),
            author: getValue('author'),
            manager: getValue('manager'),
            workType: workType,
            workersCount: parseInt(getValue('workersCount')) || 0,
            workDetails: getValue('workDetails'),
            workerNames: (getValue('workerNames') || "").split(',').map(s => s.trim()),
            potentialRiskFactors: getValue('potentialRiskFactors'),
            countermeasures: getValue('countermeasures'),
            majorRiskFactors: getValue('majorRiskFactors'),
            followUpPhoto: followUpPhotoPath,
            safetyChecklist: checklistItems,
            photos: uploadedPhotos,
            attachments: uploadedAttachments,
            signature: signaturePath,
            remarks: getValue('remarks'),
            equipment: []
        };

        let url = '/api/safety-log';
        let method = 'POST';
        if (currentEditingId) {
            url = `/api/safety-log/${currentEditingId}`;
            method = 'PUT';
        }

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(logData)
        });

        if (response.ok) {
            alert("성공적으로 저장되었습니다!");
            window.location.href = "/log-list.html";
        } else {
            alert("저장 실패: " + await response.text());
        }
    } catch (error) {
        alert("오류: " + error.message);
    } finally {
        if (submitBtn instanceof HTMLButtonElement) {
            submitBtn.disabled = false;
            submitBtn.textContent = '안전일지 저장 완료';
        }
    }
}