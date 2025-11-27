let currentEditingId = null;

const checklistData = {
    electric: ["작업 전 전원 차단 및 검전 실시 여부", "절연 장갑 등 감전 방지용 보호구 착용", "가설 분전반 누전 차단기 작동 상태 확인", "전선 피복 손상 및 충전부 노출 여부"],
    partition: ["자재 절단 시 보안경 및 방진 마스크 착용", "작업 발판(우마) 안전 상태 및 전도 방지 조치", "석고보드/자재 적재 시 무너짐 방지 조치", "타정총(타카) 사용 시 오발 사고 주의"],
    scaffold: ["비계 기둥 및 띠장 체결 상태(클램프) 확인", "작업 발판 고정 및 틈새 없는지 확인", "추락 방지망 및 안전 난간대 설치 상태", "비계 상부 적재 하중 준수 여부"],
    plumbing: ["용접/절단 작업 시 화기 감시자 배치 확인", "밀폐 공간 작업 시 산소 농도 측정 및 환기", "파이프 절단면 날카로움에 의한 베임 주의", "중량물 취급 시 올바른 운반 자세 준수"],
    duct: ["고소 작업 시 안전대(벨트) 착용 및 체결", "덕트 절단면 장갑 착용 및 손 베임 주의", "천장 앵커 및 지지대 고정 상태 확인", "TL(고소작업대) 사용 시 아웃트리거 설치 확인"],
    steel: ["철골 상부 이동 시 안전대 부착 설비(생명줄) 확인", "인양 와이어 로프 및 샤클 체결 상태 점검", "볼트 체결 작업 시 낙하물 방지 조치", "강풍(10m/s 이상) 시 작업 중지 기준 준수"],
    form: ["거푸집 동바리 수직/수평 고정 상태 확인", "자재 인양 및 해체 시 하부 통제 실시", "폼 타이/못 등에 찔림 사고 방지 조치", "콘크리트 타설 압력 견딤 상태 점검"],
    welding: ["소화기 비치 및 불티 비산 방지포 설치", "용접 흄(가스) 배출을 위한 환기 상태", "용접기 외함 접지 및 자동 전격 방지기 작동", "용접 면/앞치마 등 화상 방지 보호구 착용"]
};

document.addEventListener('DOMContentLoaded', () => {
    const workTypeSelect = document.getElementById('workType');
    if(workTypeSelect) workTypeSelect.addEventListener('change', updateChecklist);

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('id')) loadLogDataForEdit(urlParams.get('id'));

    initSignaturePad();
    document.getElementById('finalSubmitBtn').addEventListener('click', saveLog);
});

function updateChecklist() {
    const workType = document.getElementById("workType").value;
    const container = document.getElementById("checklistContainer");
    container.innerHTML = "";
    if (!workType || !checklistData[workType]) {
        container.innerHTML = '<p style="color: #888; margin: 0;">공종을 선택하면 점검 항목이 나타납니다.</p>';
        return;
    }
    checklistData[workType].forEach((item, index) => {
        const div = document.createElement("div");
        div.style.marginBottom = "8px";
        div.innerHTML = `<label style="display: flex; align-items: center; cursor: pointer;"><input type="checkbox" class="safety-item" value="${item}" checked style="width: 18px; height: 18px; margin-right: 10px;">${item}</label>`;
        container.appendChild(div);
    });
}

async function saveLog() {
    const submitBtn = document.getElementById('finalSubmitBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = '저장 중...';

    try {
        const uploadedPhotos = await uploadFiles('photosInput');
        const uploadedAttachments = await uploadFiles('attachmentsInput');

        let followUpPhotoPath = "";
        const followUpInput = document.getElementById('followUpPhotoInput');
        if(followUpInput.files.length > 0) followUpPhotoPath = await uploadSingleFile(followUpInput.files[0]);

        let signaturePath = "";
        const sigInput = document.getElementById('signatureInput');
        if (sigInput.files.length > 0) signaturePath = await uploadSingleFile(sigInput.files[0]);
        else if (window.signatureFile) signaturePath = await uploadSingleFile(window.signatureFile);

        const checklistDtos = [];
        document.querySelectorAll('.safety-item').forEach(box => {
            if (box.checked) checklistDtos.push({ item: box.value, status: "양호" });
        });

        const logData = {
            company: document.getElementById('company').value,
            logDate: document.getElementById('inspectionDate').value,
            weather: document.getElementById('weather').value,
            location: document.getElementById('location').value,
            author: document.getElementById('author').value,
            manager: document.getElementById('manager').value,
            workType: document.getElementById('workType').value,
            workersCount: parseInt(document.getElementById('workersCount').value) || 0,
            workDetails: document.getElementById('workDetails').value,
            workerNames: (document.getElementById('workerNames').value || "").split(',').map(s => s.trim()),

            potentialRiskFactors: document.getElementById('potentialRiskFactors').value,
            countermeasures: document.getElementById('countermeasures').value,
            majorRiskFactors: document.getElementById('majorRiskFactors').value,
            followUpPhoto: followUpPhotoPath,

            safetyChecklist: checklistDtos,
            remarks: document.getElementById('remarks').value,
            photos: uploadedPhotos,
            attachments: uploadedAttachments,
            signature: signaturePath,
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
            alert("저장되었습니다!");
            window.location.href = "/log-list.html";
        } else {
            alert("저장 실패: " + await response.text());
        }
    } catch (error) {
        console.error(error);
        alert("오류: " + error.message);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = '안전일지 최종 저장';
    }
}

async function loadLogDataForEdit(id) {
    try {
        const response = await fetch(`/api/safety-log/${id}`);
        if (!response.ok) throw new Error("조회 실패");
        const data = await response.json();
        currentEditingId = id;
        document.getElementById('finalSubmitBtn').textContent = "안전일지 수정하기";

        document.getElementById('inspectionDate').value = data.logDate;
        document.getElementById('company').value = data.company;
        document.getElementById('weather').value = data.weather;
        document.getElementById('location').value = data.location;
        document.getElementById('author').value = data.author;
        document.getElementById('manager').value = data.manager;
        document.getElementById('workersCount').value = data.workersCount;
        document.getElementById('workDetails').value = data.workDetails;
        document.getElementById('workerNames').value = (data.workerNames || []).join(', ');
        document.getElementById('remarks').value = data.remarks;

        document.getElementById('potentialRiskFactors').value = data.potentialRiskFactors || "";
        document.getElementById('countermeasures').value = data.countermeasures || "";
        document.getElementById('majorRiskFactors').value = data.majorRiskFactors || "";

        if (data.workType) {
            document.getElementById('workType').value = data.workType;
            updateChecklist();
            const savedItems = (data.safetyChecklist || []).map(obj => obj.item);
            document.querySelectorAll('.safety-item').forEach(box => {
                box.checked = savedItems.includes(box.value);
            });
        }
    } catch (e) {
        alert("로딩 실패: " + e.message);
    }
}

async function uploadFiles(inputId) {
    const files = document.getElementById(inputId).files;
    const paths = [];
    for (const file of files) paths.push(await uploadSingleFile(file));
    return paths;
}
async function uploadSingleFile(file) {
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch('/api/files/upload', { method: 'POST', body: formData });
    if (!res.ok) throw new Error("업로드 실패");
    const json = await res.json();
    return json.filePath;
}
function initSignaturePad() {
    const canvas = document.getElementById('signature-pad');
    if (!canvas) return;
    const signaturePad = new SignaturePad(canvas, { backgroundColor: 'rgb(255, 255, 255)' });
    function resizeCanvas() {
        const ratio = Math.max(window.devicePixelRatio || 1, 1);
        canvas.width = canvas.offsetWidth * ratio;
        canvas.height = canvas.offsetHeight * ratio;
        canvas.getContext("2d").scale(ratio, ratio);
        signaturePad.clear();
    }
    window.addEventListener("resize", resizeCanvas);
    resizeCanvas();
    document.getElementById('sig-clear-btn').addEventListener('click', () => signaturePad.clear());
    document.getElementById('sig-save-btn').addEventListener('click', () => {
        if (signaturePad.isEmpty()) return alert("서명해 주세요.");
        const dataURL = signaturePad.toDataURL("image/png");
        const arr = dataURL.split(','), mime = arr[0].match(/:(.*?);/)[1];
        const bstr = atob(arr[1]); let n = bstr.length; const u8arr = new Uint8Array(n);
        while(n--){ u8arr[n] = bstr.charCodeAt(n); }
        window.signatureFile = new File([u8arr], "signature.png", { type: mime });
        alert("서명이 임시 저장되었습니다.");
    });
}