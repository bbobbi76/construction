// construction.js
/* global SignaturePad */

/**
 * [전역 변수]
 * - currentEditingId: 수정 모드일 경우 공사일지 ID를 저장합니다.
 * - signaturePad: 서명 패드 객체
 * - signatureFile: 서명 후 변환된 이미지 파일 객체
 */
let currentEditingId = null;
let signaturePad = null;
let signatureFile = null;

/**
 * 1. 초기화 및 이벤트 리스너 등록
 */
document.addEventListener('DOMContentLoaded', () => {

    // 1-1. 날짜 및 날씨 자동 입력
    autoFillDateAndWeather();

    // 1-2. URL 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    const logId = urlParams.get('id');

    if (logId) {
        // ID가 있으면 [수정 모드]
        loadLogDataForEdit(logId).catch(console.error);
    } else {
        // ID가 없으면 [작성 모드]
        autoCheckLastLog().catch(console.error);
    }

    // 1-3. 서명 패드 초기화
    initSignaturePad();

    // 1-4. 버튼 이벤트 등록
    const submitBtn = document.getElementById('finalSubmitBtn');
    if (submitBtn) submitBtn.addEventListener('click', saveLog);

    const aiBtn = document.getElementById('btn-analyze-ai');
    if (aiBtn) aiBtn.addEventListener('click', runAiAnalysis);

    // 1-5. PDF 다운로드 버튼
    const pdfBtn = document.getElementById('btn-download-pdf');
    if (pdfBtn && logId) {
        pdfBtn.style.display = 'inline-block';
        pdfBtn.addEventListener('click', () => {
            window.location.href = `/api/construction-log/${logId}/pdf`;
        });
    }

    // 1-6. 공종 선택 로직
    const workTypeSelect = document.getElementById('workTypeSelect');
    const workTypeDirect = document.getElementById('workTypeDirect');

    // IntelliJ Null Check & Type Check
    if (workTypeSelect instanceof HTMLSelectElement && workTypeDirect instanceof HTMLInputElement) {
        workTypeSelect.addEventListener('change', function() {
            // @ts-ignore (this.value 접근 허용)
            if (this.value === 'direct') {
                workTypeDirect.style.display = 'block';
                workTypeDirect.focus();
            } else {
                workTypeDirect.style.display = 'none';
                workTypeDirect.value = '';
            }
        });
    }
});

// =================================================================================
//                                2. 데이터 로딩 및 폼 채우기
// =================================================================================

async function autoCheckLastLog() {
    try {
        const response = await fetch('/api/construction-log/last');
        if (response.status === 204 || !response.ok) return;

        if (confirm("📢 알림\n가장 최근에 작성한 일지 내용이 있습니다.\n자동으로 불러오시겠습니까?")) {
            const dto = await response.json();
            fillFormWithData(dto);
        }
    } catch (e) {
        console.error("전일 데이터 확인 중 오류:", e);
    }
}

async function loadLogDataForEdit(id) {
    try {
        const response = await fetch(`/api/construction-log/${id}`);
        if (!response.ok) throw new Error('조회 실패');

        const dto = await response.json();

        currentEditingId = id;
        const submitBtn = document.getElementById('finalSubmitBtn');
        if (submitBtn) submitBtn.textContent = '수정 내용 저장';

        fillFormWithData(dto);

        const dateInput = document.getElementById('logDate');
        if (dateInput instanceof HTMLInputElement) dateInput.value = dto.logDate;

    } catch (error) {
        alert('데이터를 불러오지 못했습니다.');
    }
}

// 헬퍼 함수: ID로 엘리먼트 찾아서 값 설정 (Null safe)
function setValueById(id, value) {
    const el = document.getElementById(id);
    if (el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement || el instanceof HTMLSelectElement) {
        el.value = value || "";
    }
}

function fillFormWithData(dto) {
    setValueById('company', dto.company);
    setValueById('location', dto.location);
    setValueById('author', dto.author);
    setValueById('manager', dto.manager);
    setValueById('workersCount', dto.workersCount);
    setValueById('workDetails', dto.workDetails);
    setValueById('remarks', dto.remarks);
    setValueById('aiWorkDescription', dto.aiWorkDescription);

    const workerNamesInput = document.getElementById('workerNames');
    if (workerNamesInput instanceof HTMLInputElement) {
        workerNamesInput.value = (dto.workerNames || []).join(', ');
    }

    // 공종 선택 처리
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

    // 테이블 초기화
    const eqTbody = document.getElementById('equipmentTbody');
    if (eqTbody) eqTbody.innerHTML = '';
    // @ts-ignore (window 함수 호출)
    (dto.equipment || []).forEach(eq => window.addEquipmentRow(eq.name, eq.count));

    const matTbody = document.getElementById('materialTbody');
    if (matTbody) matTbody.innerHTML = '';
    // @ts-ignore (window 함수 호출)
    (dto.materials || []).forEach(mat => window.addMaterialRow(mat.name, mat.quantity));
}

function autoFillDateAndWeather() {
    const dateInput = document.getElementById('logDate');
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

// =================================================================================
//                                3. AI 및 서명 기능
// =================================================================================

async function runAiAnalysis() {
    const fileInput = document.getElementById('photosInput');
    const aiTextArea = document.getElementById('aiWorkDescription');
    const btn = document.getElementById('btn-analyze-ai');

    if (!(fileInput instanceof HTMLInputElement) || !fileInput.files || fileInput.files.length === 0) {
        return alert("사진을 먼저 업로드해주세요.");
    }

    if (!btn || !(aiTextArea instanceof HTMLTextAreaElement)) return;

    const originalText = btn.textContent;
    btn.disabled = true;
    btn.textContent = "분석 중...";
    aiTextArea.placeholder = "AI가 현장 상황을 분석하고 있습니다...";

    try {
        const formData = new FormData();
        formData.append("file", fileInput.files[0]);

        const response = await fetch('/api/construction-log/analyze-photo', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            const data = await response.json();
            aiTextArea.value = data.description;
        } else {
            alert("분석 실패: 서버 오류");
        }
    } catch (error) {
        alert("오류 발생");
    } finally {
        btn.disabled = false;
        btn.textContent = originalText;
    }
}

function initSignaturePad() {
    const canvas = document.getElementById('signature-pad');
    if (!(canvas instanceof HTMLCanvasElement)) return;

    signaturePad = new SignaturePad(canvas, { backgroundColor: 'rgb(255, 255, 255)' });

    function resizeCanvas() {
        const ratio = Math.max(window.devicePixelRatio || 1, 1);
        // @ts-ignore
        canvas.width = canvas.offsetWidth * ratio;
        // @ts-ignore
        canvas.height = canvas.offsetHeight * ratio;
        canvas.getContext("2d").scale(ratio, ratio);
        if (signaturePad) signaturePad.clear();
    }
    window.addEventListener("resize", resizeCanvas);
    resizeCanvas();

    const clearBtn = document.getElementById('sig-clear-btn');
    if (clearBtn) clearBtn.addEventListener('click', () => {
        if (signaturePad) signaturePad.clear();
    });

    const saveBtn = document.getElementById('sig-save-btn');
    if (saveBtn) {
        saveBtn.addEventListener('click', () => {
            if (!signaturePad || signaturePad.isEmpty()) return alert("서명해 주세요.");

            const dataURL = signaturePad.toDataURL("image/png");
            const arr = dataURL.split(',');
            const mime = arr[0].match(/:(.*?);/)[1];
            const bstr = atob(arr[1]);
            let n = bstr.length;
            const u8arr = new Uint8Array(n);

            while (n--) { u8arr[n] = bstr.charCodeAt(n); }

            signatureFile = new File([u8arr], "signature.png", { type: mime });
            alert("서명이 임시 저장되었습니다.");
        });
    }
}

// =================================================================================
//                                4. 테이블 동적 관리
// =================================================================================

// @ts-ignore
window.removeRow = function(button) {
    button.closest('tr').remove();
};

// @ts-ignore
window.addEquipmentRow = function(name = '', count = 1) {
    const tbody = document.getElementById('equipmentTbody');
    if (!tbody) return;
    // @ts-ignore
    const newRow = tbody.insertRow();
    newRow.innerHTML = `
        <td><input type="text" class="eq-name" value="${name}"></td>
        <td><input type="number" class="eq-count" value="${count}"></td>
        <td><button type="button" class="row-del-btn" onclick="removeRow(this)">삭제</button></td>
    `;
};

// @ts-ignore
window.addMaterialRow = function(name = '', quantity = '') {
    const tbody = document.getElementById('materialTbody');
    if (!tbody) return;
    // @ts-ignore
    const newRow = tbody.insertRow();
    newRow.innerHTML = `
        <td><input type="text" class="mat-name" value="${name}"></td>
        <td><input type="text" class="mat-quantity" value="${quantity}"></td>
        <td><button type="button" class="row-del-btn" onclick="removeRow(this)">삭제</button></td>
    `;
};

// =================================================================================
//                                5. 저장 로직
// =================================================================================

async function uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch('/api/files/upload', {
        method: 'POST',
        body: formData
    });

    if (!response.ok) throw new Error("Upload failed");
    return (await response.json()).filePath;
}

async function saveLog() {
    const dateInput = document.getElementById('logDate');
    const locationInput = document.getElementById('location');

    if (!(dateInput instanceof HTMLInputElement) || !(locationInput instanceof HTMLInputElement)) return;

    if (!dateInput.value || !locationInput.value) return alert('필수 항목(작업일, 위치)을 입력해주세요.');

    let workType = '';
    const workSelect = document.getElementById('workTypeSelect');
    const workDirect = document.getElementById('workTypeDirect');

    if (workSelect instanceof HTMLSelectElement) workType = workSelect.value;
    if (workType === 'direct' && workDirect instanceof HTMLInputElement) {
        workType = workDirect.value;
    }

    const submitBtn = document.getElementById('finalSubmitBtn');
    if (submitBtn instanceof HTMLButtonElement) {
        submitBtn.disabled = true;
        submitBtn.textContent = '저장 중...';
    }

    try {
        const uploadedFilePaths = { photos: [], attachments: [], signature: "" };

        // 사진 업로드
        const photosInput = document.getElementById('photosInput');
        if (photosInput instanceof HTMLInputElement && photosInput.files) {
            for (const file of photosInput.files) {
                uploadedFilePaths.photos.push(await uploadFile(file));
            }
        }

        // 첨부파일 업로드
        const attachInput = document.getElementById('attachmentsInput');
        if (attachInput instanceof HTMLInputElement && attachInput.files) {
            for (const file of attachInput.files) {
                uploadedFilePaths.attachments.push(await uploadFile(file));
            }
        }

        // 서명 업로드
        const sigFileInput = document.getElementById('signatureInput');
        if (sigFileInput instanceof HTMLInputElement && sigFileInput.files.length > 0) {
            uploadedFilePaths.signature = await uploadFile(sigFileInput.files[0]);
        } else if (signatureFile) {
            uploadedFilePaths.signature = await uploadFile(signatureFile);
        }

        const getValue = (id) => {
            const el = document.getElementById(id);
            if (el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement) return el.value;
            return "";
        };

        const equipmentList = [];
        document.querySelectorAll('#equipmentTbody tr').forEach(row => {
            const nameEl = row.querySelector('.eq-name');
            const countEl = row.querySelector('.eq-count');
            if (nameEl instanceof HTMLInputElement && countEl instanceof HTMLInputElement) {
                if (nameEl.value) {
                    equipmentList.push({ name: nameEl.value, count: parseInt(countEl.value) || 0 });
                }
            }
        });

        const materialList = [];
        document.querySelectorAll('#materialTbody tr').forEach(row => {
            const nameEl = row.querySelector('.mat-name');
            const qtyEl = row.querySelector('.mat-quantity');
            if (nameEl instanceof HTMLInputElement && qtyEl instanceof HTMLInputElement) {
                if (nameEl.value) {
                    materialList.push({ name: nameEl.value, quantity: qtyEl.value });
                }
            }
        });

        const logData = {
            company: getValue('company'),
            logDate: dateInput.value,
            weather: getValue('weather'),
            location: locationInput.value,
            manager: getValue('manager'),
            workType: workType,
            workersCount: parseInt(getValue('workersCount')) || 0,
            workDetails: getValue('workDetails'),
            aiWorkDescription: getValue('aiWorkDescription'),
            workerNames: (getValue('workerNames') || "").split(',').map(s => s.trim()).filter(s => s),
            remarks: getValue('remarks'),
            equipment: equipmentList,
            materials: materialList,
            photos: uploadedFilePaths.photos,
            attachments: uploadedFilePaths.attachments,
            signature: uploadedFilePaths.signature
        };

        let url = '/api/construction-log';
        let method = 'POST';

        if (currentEditingId) {
            url = `/api/construction-log/${currentEditingId}`;
            method = 'PUT';
        }

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(logData),
        });

        if (response.ok) {
            alert('성공적으로 저장되었습니다!');
            window.location.href = '/log-list.html';
        } else {
            alert(`저장 실패: ${await response.text()}`);
        }
    } catch (error) {
        console.error(error);
        alert('오류가 발생했습니다.');
    } finally {
        if (submitBtn instanceof HTMLButtonElement) {
            submitBtn.disabled = false;
            submitBtn.textContent = '공사일지 저장 완료';
        }
    }
}