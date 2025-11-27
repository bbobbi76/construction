// construction.js

let currentEditingId = null;

document.addEventListener('DOMContentLoaded', () => {
    // 1. 조회 모드 확인
    const urlParams = new URLSearchParams(window.location.search);
    const logId = urlParams.get('id');
    if (logId) {
        loadLogDataForEdit(logId);
    }

    // 2. 서명 패드 초기화
    initSignaturePad();

    // 3. 저장 버튼 이벤트
    document.getElementById('finalSubmitBtn').addEventListener('click', saveLog);
});

// --- 서명 패드 로직 (함수로 분리) ---
let signaturePad;
let signatureFile = null;

function initSignaturePad() {
    const canvas = document.getElementById('signature-pad');
    if (!canvas) return;
    signaturePad = new SignaturePad(canvas, { backgroundColor: 'rgb(255, 255, 255)' });

    function resizeCanvas() {
        const ratio = Math.max(window.devicePixelRatio || 1, 1);
        canvas.width = canvas.offsetWidth * ratio;
        canvas.height = canvas.offsetHeight * ratio;
        canvas.getContext("2d").scale(ratio, ratio);
        signaturePad.clear();
    }
    resizeCanvas();
    window.addEventListener("resize", resizeCanvas);

    document.getElementById('sig-clear-btn').addEventListener('click', () => signaturePad.clear());
    document.getElementById('sig-save-btn').addEventListener('click', () => {
        if (signaturePad.isEmpty()) return alert("먼저 서명을 해주세요.");
        const dataURL = signaturePad.toDataURL("image/png");
        const arr = dataURL.split(','), mime = arr[0].match(/:(.*?);/)[1];
        const bstr = atob(arr[1]); let n = bstr.length; const u8arr = new Uint8Array(n);
        while(n--){ u8arr[n] = bstr.charCodeAt(n); }
        signatureFile = new File([u8arr], "signature.png", { type: mime });
        alert("저장되었습니다.");
    });
}

// --- 행 추가/삭제 헬퍼 ---
window.removeRow = function(button) { button.closest('tr').remove(); }
window.addEquipmentRow = function(name = '', count = 1) {
    const tbody = document.getElementById('equipmentTbody');
    const newRow = tbody.insertRow();
    newRow.innerHTML = `
        <td><input type="text" class="eq-name" placeholder="장비명" value="${name}"></td>
        <td><input type="number" class="eq-count" value="${count}"></td>
        <td><button type="button" class="row-del-btn" onclick="removeRow(this)">삭제</button></td>
    `;
}
window.addMaterialRow = function(name = '', quantity = '') {
    const tbody = document.getElementById('materialTbody');
    const newRow = tbody.insertRow();
    newRow.innerHTML = `
        <td><input type="text" class="mat-name" placeholder="자재명" value="${name}"></td>
        <td><input type="text" class="mat-quantity" placeholder="수량" value="${quantity}"></td>
        <td><button type="button" class="row-del-btn" onclick="removeRow(this)">삭제</button></td>
    `;
}

// --- 파일 업로드 헬퍼 ---
async function uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file);
    const response = await fetch('/api/files/upload', { method: 'POST', body: formData });
    if (!response.ok) throw new Error(`Upload failed: ${response.status}`);
    const result = await response.json();
    return result.filePath;
}

// --- ★ 저장 함수 (핵심 수정) ---
async function saveLog() {
    // 필수값 체크
    const logDate = document.getElementById('logDate').value;
    const location = document.getElementById('location').value;
    if (!logDate || !location) return alert('작업일과 현장 위치는 필수입니다.');

    const submitBtn = document.getElementById('finalSubmitBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = '저장 중...';

    try {
        // 1. 파일 업로드
        const uploadedFilePaths = { photos: [], attachments: [], signature: "" };

        const photoFiles = document.getElementById('photosInput').files;
        for (const file of photoFiles) uploadedFilePaths.photos.push(await uploadFile(file));

        const attachmentFiles = document.getElementById('attachmentsInput').files;
        for (const file of attachmentFiles) uploadedFilePaths.attachments.push(await uploadFile(file));

        const sigFileInput = document.getElementById('signatureInput');
        if (sigFileInput.files.length > 0) uploadedFilePaths.signature = await uploadFile(sigFileInput.files[0]);
        else if (signatureFile) uploadedFilePaths.signature = await uploadFile(signatureFile);

        // 2. 리스트 데이터 수집
        const parseListString = (str) => (str || "").split(',').map(s => s.trim()).filter(s => s);

        const equipmentList = [];
        document.querySelectorAll('#equipmentTbody tr').forEach(row => {
            const name = row.querySelector('.eq-name').value;
            const count = parseInt(row.querySelector('.eq-count').value) || 0;
            if (name) equipmentList.push({ name, count });
        });

        const materialList = [];
        document.querySelectorAll('#materialTbody tr').forEach(row => {
            const name = row.querySelector('.mat-name').value;
            const quantity = row.querySelector('.mat-quantity').value;
            if (name) materialList.push({ name, quantity });
        });

        // 3. 데이터 조립 (AI 필드 포함)
        const logData = {
            company: document.getElementById('company').value,
            logDate: logDate,
            weather: document.getElementById('weather').value,
            location: location,
            // author: document.getElementById('author').value, // (백엔드 Principal에서 처리하므로 생략 가능)
            manager: document.getElementById('manager').value,

            workType: document.getElementById('workType').value,
            workersCount: parseInt(document.getElementById('workersCount').value) || 0,
            workDetails: document.getElementById('workDetails').value,

            // ★ [추가됨] AI 분석 내용
            aiWorkDescription: document.getElementById('aiWorkDescription').value,

            workerNames: parseListString(document.getElementById('workerNames').value),
            remarks: document.getElementById('remarks').value,

            equipment: equipmentList,
            materials: materialList,
            photos: uploadedFilePaths.photos,
            attachments: uploadedFilePaths.attachments,
            signature: uploadedFilePaths.signature
        };

        // 4. 서버 전송
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
            alert('저장되었습니다!');
            window.location.href = '/log-list.html';
        } else {
            alert(`실패: ${await response.text()}`);
        }

    } catch (error) {
        console.error(error);
        alert('오류 발생');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = '공사일지 최종 저장';
    }
}

// --- ★ 조회 함수 (핵심 수정) ---
async function loadLogDataForEdit(id) {
    try {
        const response = await fetch(`/api/construction-log/${id}`);
        if (!response.ok) throw new Error('조회 실패');
        const dto = await response.json();

        currentEditingId = id;
        document.getElementById('finalSubmitBtn').textContent = '공사일지 수정하기';

        // 필드 채우기
        document.getElementById('logDate').value = dto.logDate;
        document.getElementById('company').value = dto.company;
        document.getElementById('weather').value = dto.weather;
        document.getElementById('location').value = dto.location;
        document.getElementById('author').value = dto.author; // (읽기 전용으로 표시됨)
        document.getElementById('manager').value = dto.manager;
        document.getElementById('workType').value = dto.workType;
        document.getElementById('workersCount').value = dto.workersCount;
        document.getElementById('workDetails').value = dto.workDetails;
        document.getElementById('workerNames').value = (dto.workerNames || []).join(', ');
        document.getElementById('remarks').value = dto.remarks;

        // ★ [추가됨] AI 분석 내용 채우기
        document.getElementById('aiWorkDescription').value = dto.aiWorkDescription || "";

        // 테이블 복원
        const eqTbody = document.getElementById('equipmentTbody');
        eqTbody.innerHTML = '';
        (dto.equipment || []).forEach(eq => addEquipmentRow(eq.name, eq.count));

        const matTbody = document.getElementById('materialTbody');
        matTbody.innerHTML = '';
        (dto.materials || []).forEach(mat => addMaterialRow(mat.name, mat.quantity));

    } catch (error) {
        alert('데이터 로딩 실패: ' + error.message);
    }
}