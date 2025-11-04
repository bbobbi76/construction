/**
 * [4단계] "제대로 된" 공사일지 폼(HTML)과 백엔드 API를 연동하는 스크립트
 *
 * - B안 (author 필드)
 * - [2단계] 파일 업로드 API (/api/files/upload)
 * - [3단계] 서명 패드 (signature_pad)
 * - [수정] 서명 좌표 밀림(resize) 문제 해결
 *
 * ...를 모두 반영한 '2-Step 저장' 로직
 */

// --- 1. 서명 패드(SignaturePad) 초기화 ---
const canvas = document.getElementById('signature-pad');
const signaturePad = new SignaturePad(canvas, {
    backgroundColor: 'rgb(255, 255, 255)' // 서명 배경을 흰색으로
});

// 🚨 --- [좌표 밀림 수정] 캔버스 리사이즈 함수 ---
/**
 * (헬퍼) CSS 크기와 캔버스 픽셀 크기를 동기화하는 함수
 * (이걸 안하면 좌표가 밀려서 옆에 그려짐)
 */
function resizeCanvas() {
    // 1. (고해상도 대비) 디바이스 픽셀 비율
    const ratio =  Math.max(window.devicePixelRatio || 1, 1);

    // 2. CSS에서 계산된 '겉모습' 크기를 읽어옴
    const cssWidth = canvas.offsetWidth;
    const cssHeight = canvas.offsetHeight;

    // 3. 캔버스의 '실제 픽셀(Attribute)' 크기를 CSS 크기에 맞게 설정
    canvas.width = cssWidth * ratio;
    canvas.height = cssHeight * ratio;

    // 4. 캔버스 2D 컨텍스트도 비율에 맞게 스케일링
    canvas.getContext("2d").scale(ratio, ratio);

    // 5. (중요) 리사이즈 후, 서명 패드에 저장된 이전 그림을 초기화
    signaturePad.clear();
}

// 1. 페이지 로드 시 1회 실행 (초기화)
resizeCanvas();

// 2. 브라우저 창 크기가 바뀔 때마다 캔버스 크기 재조정
window.addEventListener("resize", resizeCanvas);
// 🚨 --- 여기까지 추가 ---


// "다시 그리기" 버튼
document.getElementById('sig-clear-btn').addEventListener('click', () => {
    signaturePad.clear();
});

// "서명 저장 (그림판 -> 파일)" 버튼
let signatureFile = null; // 서명 파일 객체를 저장할 변수
document.getElementById('sig-save-btn').addEventListener('click', () => {
    if (signaturePad.isEmpty()) {
        alert("먼저 서명을 해주세요.");
    } else {
        // 서명(Base64)을 PNG 파일 객체(Blob)로 변환
        const dataURL = signaturePad.toDataURL("image/png");
        const blob = dataURLToBlob(dataURL);

        // 가짜 파일명(signature.png)을 가진 File 객체로 생성
        signatureFile = new File([blob], "signature.png", { type: "image/png" });
        alert("저장되었습니다.");
    }
});

/**
 * (헬퍼) 서명 패드(Base64) 데이터를 File 객체로 변환
 */
function dataURLToBlob(dataURL) {
    const parts = dataURL.split(';base64,');
    const contentType = parts[0].split(':')[1];
    const raw = window.atob(parts[1]);
    const rawLength = raw.length;
    const uInt8Array = new Uint8Array(rawLength);
    for (let i = 0; i < rawLength; ++i) {
        uInt8Array[i] = raw.charCodeAt(i);
    }
    return new Blob([uInt8Array], { type: contentType });
}


// --- 2. "최종 저장" 버튼 이벤트 ---
document.getElementById('finalSubmitBtn').addEventListener('click', saveLog);


/**
 * (공통) 테이블 행 삭제 함수
 * @param {HTMLButtonElement} button - 클릭된 '삭제' 버튼
 */
function removeRow(button) {
    // 버튼의 가장 가까운 부모 <tr>을 찾아서 삭제
    button.closest('tr').remove();
}

/**
 * "장비 행 추가" 버튼
 */
function addEquipmentRow() {
    const tbody = document.getElementById('equipmentTbody');
    const newRow = tbody.insertRow(); // 새 <tr> 생성
    newRow.innerHTML = `
        <td><input type="text" class="eq-name" placeholder="장비명"></td>
        <td><input type="number" class="eq-count" value="1"></td>
        <td><button type="button" class="row-del-btn" onclick="removeRow(this)">삭제</button></td>
    `;
}

/**
 * "자재 행 추가" 버튼
 */
function addMaterialRow() {
    const tbody = document.getElementById('materialTbody');
    const newRow = tbody.insertRow(); // 새 <tr> 생성
    newRow.innerHTML = `
        <td><input type="text" class="mat-name" placeholder="자재명"></td>
        <td><input type="text" class="mat-quantity" placeholder="수량 (예: 5톤)"></td>
        <td><button type="button" class="row-del-btn" onclick="removeRow(this)">삭제</button></td>
    `;
}


/**
 * (핵심) [2단계]에서 만든 파일 업로드 API(/api/files/upload)를 호출하는 함수
 * @param {File} file - 업로드할 파일 객체
 * @returns {Promise<String>} - 서버에 저장된 파일 경로 (예: "/uploads/uuid_photo.jpg")
 */
async function uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file); // Controller의 @RequestParam("file")과 키 일치

    console.log(`파일 업로드 시도: ${file.name}`);

    try {
        const response = await fetch('/api/files/upload', {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            throw new Error(`파일 업로드 실패 (HTTP ${response.status})`);
        }

        const result = await response.json();
        console.log(`파일 업로드 성공: ${result.filePath}`);
        return result.filePath; // {"filePath": "..."} 에서 경로 값만 반환

    } catch (error) {
        console.error('File Upload Error:', error);
        alert(`Error: ${file.name} 업로드 중 오류 발생.`);
        throw error; // 오류를 상위로 전파
    }
}


/**
 * (메인) "최종 저장" 버튼 클릭 시 실행되는 함수
 * '2-Step 저장' 로직
 */
async function saveLog() {

    // (로딩 중 버튼 비활성화 - 선택 사항)
    const submitBtn = document.getElementById('finalSubmitBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = '저장 중... (1/2)';

    // --- (Step 1) 파일 업로드 및 경로 확보 ---

    const uploadedFilePaths = {
        photos: [],
        attachments: [],
        signature: "" // 서명은 파일 1개
    };

    try {
        // (A) 현장 사진 (photosInput) 업로드 (여러 개)
        const photoFiles = document.getElementById('photosInput').files;
        for (const file of photoFiles) {
            const path = await uploadFile(file);
            uploadedFilePaths.photos.push(path);
        }

        // (B) 첨부 파일 (attachmentsInput) 업로드 (여러 개)
        const attachmentFiles = document.getElementById('attachmentsInput').files;
        for (const file of attachmentFiles) {
            const path = await uploadFile(file);
            uploadedFilePaths.attachments.push(path);
        }

        // (C) 서명 (signatureInput 또는 signaturePad) 업로드 (1개)
        const sigFileInput = document.getElementById('signatureInput');

        if (sigFileInput.files.length > 0) {
            // (C-1) 서명 '파일'을 업로드한 경우
            const path = await uploadFile(sigFileInput.files[0]);
            uploadedFilePaths.signature = path;
        } else if (signatureFile) {
            // (C-2) '그림판'에서 "서명 저장" 버튼을 눌러둔 경우
            const path = await uploadFile(signatureFile);
            uploadedFilePaths.signature = path;
        } else if (!signaturePad.isEmpty()) {
            // (C-3) '그림판'에 그림만 그리고 "서명 저장"을 안 누른 경우
            alert("저장 버튼을 먼저 눌러주세요.");
            throw new Error("서명 파일 변환 필요");
        }

    } catch (error) {
        // (Step 1) 파일 업로드 중 1개라도 실패하면, (Step 2) JSON 저장을 시도하지 않음
        alert("파일 업로드에 실패했습니다. 저장을 중단합니다.");
        submitBtn.disabled = false;
        submitBtn.textContent = '공사일지 최종 저장';
        return; // 함수 종료
    }


    // --- (Step 2) '글자(Text)' 데이터 + (Step 1)의 '경로'를 JSON으로 조립 ---

    submitBtn.textContent = '저장 중... (2/2)';

    // (A) List<String> 타입 변환 헬퍼 (콤마로 분리)
    const parseListString = (rawString) => {
        if (!rawString) return [];
        return rawString.split(',').map(s => s.trim()).filter(s => s);
    };

    // (B) 장비 테이블(List<EquipmentDto>) 읽기
    const equipmentList = [];
    document.querySelectorAll('#equipmentTbody tr').forEach(row => {
        const name = row.querySelector('.eq-name').value;
        const count = parseInt(row.querySelector('.eq-count').value) || 0;
        if (name) { equipmentList.push({ name: name, count: count }); }
    });

    // (C) 자재 테이블(List<MaterialDto>) 읽기
    const materialList = [];
    document.querySelectorAll('#materialTbody tr').forEach(row => {
        const name = row.querySelector('.mat-name').value;
        const quantity = row.querySelector('.mat-quantity').value;
        if (name) { materialList.push({ name: name, quantity: quantity }); }
    });

    // (D) 최종 JSON 객체 (B안 'author' + 파일 경로 포함)
    const logData = {
        company: document.getElementById('company').value,
        logDate: document.getElementById('logDate').value,
        weather: document.getElementById('weather').value,
        location: document.getElementById('location').value,

        author: document.getElementById('author').value, // 🚨 [B안] 작성자
        manager: document.getElementById('manager').value, // 🚨 [B안] 관리자

        workType: document.getElementById('workType').value,
        workersCount: parseInt(document.getElementById('workersCount').value) || 0,
        workDetails: document.getElementById('workDetails').value,
        remarks: document.getElementById('remarks').value,

        // 글자(콤마) -> List<String>
        workerNames: parseListString(document.getElementById('workerNames').value),

        // 테이블 -> List<DTO>
        equipment: equipmentList,
        materials: materialList,

        // 🚨 [신규] (Step 1)에서 업로드하고 받아온 '경로'들
        photos: uploadedFilePaths.photos,
        attachments: uploadedFilePaths.attachments,
        signature: uploadedFilePaths.signature
    };

    // (디버깅)
    console.log('JSON으로 변환될 최종 데이터 (Step 2):', JSON.stringify(logData, null, 2));


    // --- (Step 2) 2주차에 만든 API로 '최종 JSON' 전송 ---
    try {
        const response = await fetch('/api/construction-log', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(logData),
        });

        if (response.ok) {
            const savedData = await response.json();
            alert('최종 저장 성공! (ID: ' + savedData.id + ')');
            window.location.reload(); // 성공 시 페이지 새로고침
        } else {
            const errorText = await response.text();
            alert(`[Step 2] 최종 저장 실패: ${errorText}`);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('[Step 2] 저장 중 네트워크 오류가 발생했습니다.');
    } finally {
        // 성공하든 실패하든 버튼 활성화
        submitBtn.disabled = false;
        submitBtn.textContent = '공사일지 최종 저장';
    }
}