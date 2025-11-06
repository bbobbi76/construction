/**
 * [4주차] 안전일지 폼(HTML)과 백엔드 API를 연동하는 스크립트
 * (construction.js의 '2-Step' 파일 업로드 로직을 기반으로 함)
 * (안전일지 고유 항목 + 공사일지 공통 항목 모두 포함)
 */

// --- 1. 서명 패드(SignaturePad) 초기화 ---
// (construction.js와 100% 동일한 코드)
const canvas = document.getElementById('signature-pad');
const signaturePad = new SignaturePad(canvas, {
    backgroundColor: 'rgb(255, 255, 255)'
});

/**
 * (헬퍼) CSS 크기와 캔버스 픽셀 크기를 동기화하는 함수
 */
function resizeCanvas() {
    const ratio =  Math.max(window.devicePixelRatio || 1, 1);
    const cssWidth = canvas.offsetWidth;
    const cssHeight = canvas.offsetHeight;
    canvas.width = cssWidth * ratio;
    canvas.height = cssHeight * ratio;
    canvas.getContext("2d").scale(ratio, ratio);
    signaturePad.clear();
}
resizeCanvas(); // 페이지 로드 시 1회 실행
window.addEventListener("resize", resizeCanvas); // 창 크기 변경 시 실행

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
        const dataURL = signaturePad.toDataURL("image/png");
        const blob = dataURLToBlob(dataURL);
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
// --- 서명 패드 코드 끝 ---


// --- 2. "최종 저장" 버튼 이벤트 ---
document.getElementById('finalSubmitBtn').addEventListener('click', saveLog);


// --- 3. 동적 테이블 관리 ---

/**
 * (공통) 테이블 행 삭제 함수
 * (construction.js와 100% 동일한 코드)
 * @param {HTMLButtonElement} button - 클릭된 '삭제' 버튼
 */
function removeRow(button) {
    button.closest('tr').remove();
}

/**
 * [고유 항목] "지적사항 행 추가" 버튼
 * (safety-log.html의 onclick="addSafetyIssueRow()"가 호출)
 */
function addSafetyIssueRow() {
    const tbody = document.getElementById('safetyIssueTbody');
    const newRow = tbody.insertRow(); // 새 <tr> 생성

    // safety-log.html의 테이블 구조에 맞게 input 클래스명 지정
    newRow.innerHTML = `
        <td><input type="text" class="issue-desc" placeholder="지적 사항"></td>
        <td><input type="text" class="issue-action" placeholder="조치 내용"></td>
        <td><input type="text" class="issue-manager" placeholder="담당자"></td>
        <td><button type="button" class="row-del-btn" onclick="removeRow(this)">삭제</button></td>
    `;
}
// --- (construction.js의 addEquipmentRow, addMaterialRow는 여기서 삭제) ---


/**
 * [공통] 파일 업로드 API(/api/files/upload)를 호출하는 함수
 * (construction.js와 100% 동일한 코드)
 */
async function uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file);

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
        return result.filePath;

    } catch (error) {
        console.error('File Upload Error:', error);
        alert(`Error: ${file.name} 업로드 중 오류 발생.`);
        throw error;
    }
}


/**
 * (메인) "최종 저장" 버튼 클릭 시 실행되는 함수
 * '2-Step 저장' 로직
 */
async function saveLog() {

    const submitBtn = document.getElementById('finalSubmitBtn');
    submitBtn.disabled = true;
    // [텍스트 변경]
    submitBtn.textContent = '저장 중... (1/2)';

    // --- (Step 1) 파일 업로드 및 경로 확보 ---
    // (construction.js와 100% 동일한 로직. HTML의 ID가 동일하기 때문)

    const uploadedFilePaths = {
        photos: [],
        attachments: [],
        signature: ""
    };

    try {
        // (A) 현장 사진 (AI 분석용)
        const photoFiles = document.getElementById('photosInput').files;
        for (const file of photoFiles) {
            const path = await uploadFile(file);
            uploadedFilePaths.photos.push(path);
        }

        // (B) 첨부 파일 (안전교육일지 등)
        const attachmentFiles = document.getElementById('attachmentsInput').files;
        for (const file of attachmentFiles) {
            const path = await uploadFile(file);
            uploadedFilePaths.attachments.push(path);
        }

        // (C) 서명
        const sigFileInput = document.getElementById('signatureInput');

        if (sigFileInput.files.length > 0) {
            const path = await uploadFile(sigFileInput.files[0]);
            uploadedFilePaths.signature = path;
        } else if (signatureFile) {
            const path = await uploadFile(signatureFile);
            uploadedFilePaths.signature = path;
        } else if (!signaturePad.isEmpty()) {
            alert("서명 '저장' 버튼을 먼저 눌러주세요.");
            throw new Error("서명 파일 변환 필요");
        }

    } catch (error) {
        alert("파일 업로드에 실패했습니다. 저장을 중단합니다.");
        submitBtn.disabled = false;
        // [텍스트 변경]
        submitBtn.textContent = '안전일지 최종 저장';
        return;
    }


    // --- (Step 2) '글자(Text)' 데이터 + (Step 1)의 '경로'를 JSON으로 조립 ---
    // 🚨 (construction.js 항목 + safety.js 항목 모두 취합) 🚨

    submitBtn.textContent = '저장 중... (2/2)';

    // (A) List<String> 타입 변환 헬퍼 (공통)
    const parseListString = (rawString) => {
        if (!rawString) return [];
        return rawString.split(',').map(s => s.trim()).filter(s => s);
    };

    // [고유 항목] (B) 체크리스트 (List<String>) 읽기
    const checklistItems = [];
    document.querySelectorAll('input[name="checklist"]:checked').forEach(chk => {
        checklistItems.push(chk.value);
    });

    // [고유 항목] (C) 지적사항 테이블(List<SafetyIssueDto>) 읽기
    const safetyIssues = [];
    document.querySelectorAll('#safetyIssueTbody tr').forEach(row => {
        const description = row.querySelector('.issue-desc').value;
        const action = row.querySelector('.issue-action').value;
        const manager = row.querySelector('.issue-manager').value;

        if (description) { // 지적 사항이 입력된 경우에만 리스트에 추가
            safetyIssues.push({
                description: description,
                action: action,
                manager: manager
            });
        }
    });
    // --- (construction.js의 equipmentList, materialList는 여기서 삭제) ---


    // (D) 최종 JSON 객체
    const logData = {
        // 1. 기본 정보 (공통)
        company: document.getElementById('company').value,
        inspectionDate: document.getElementById('inspectionDate').value, // (ID 주의: logDate -> inspectionDate)
        weather: document.getElementById('weather').value,
        location: document.getElementById('location').value,

        // 2. 담당자 정보 (공통)
        author: document.getElementById('author').value,
        manager: document.getElementById('manager').value,

        // 3. 작업 현황 (공통)
        workType: document.getElementById('workType').value,
        workersCount: parseInt(document.getElementById('workersCount').value) || 0,
        workDetails: document.getElementById('workDetails').value,
        workerNames: parseListString(document.getElementById('workerNames').value),

        // 4. 안전 점검 (고유)
        checklistItems: checklistItems, // (B)에서 수집
        riskFactors: document.getElementById('riskFactors').value,

        // 5. 지적 사항 (고유)
        safetyIssues: safetyIssues, // (C)에서 수집

        // 6. 파일 및 서명 (공통)
        remarks: document.getElementById('remarks').value,
        photos: uploadedFilePaths.photos, // (AI 분석 대상)
        attachments: uploadedFilePaths.attachments,
        signature: uploadedFilePaths.signature
    };

    console.log('JSON으로 변환될 최종 데이터 (Step 2):', JSON.stringify(logData, null, 2));


    // --- (Step 2) 2주차에 만든 API로 '최종 JSON' 전송 ---
    try {
        // 🚨 [API 엔드포인트 변경]
        const response = await fetch('/api/safety-logs', { // (construction-log -> safety-logs)
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(logData),
        });

        if (response.ok) {
            const savedData = await response.json();
            // [텍스트 변경]
            alert('안전일지 저장 성공! (ID: ' + savedData.id + ')');
            window.location.reload();
        } else {
            const errorText = await response.text();
            alert(`[Step 2] 최종 저장 실패: ${errorText}`);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('[Step 2] 저장 중 네트워크 오류가 발생했습니다.');
    } finally {
        submitBtn.disabled = false;
        // [텍스트 변경]
        submitBtn.textContent = '안전일지 최종 저장';
    }
}