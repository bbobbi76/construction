//  현재 수정 중인 ID (null이면 신규 작성)
let currentEditingId = null;

document.addEventListener('DOMContentLoaded', () => {

    //  '조회' 모드 확인
    const urlParams = new URLSearchParams(window.location.search);
    const logId = urlParams.get('id');
    if (logId) {
        loadLogDataForEdit(logId);
    }
    //  '조회' 모드 확인 끝

    //  서명 패드 초기화
    const canvas = document.getElementById('signature-pad');
    if (!canvas) return;
    const signaturePad = new SignaturePad(canvas, {
        backgroundColor: 'rgb(255, 255, 255)'
    });

    function resizeCanvas() {
        const ratio =  Math.max(window.devicePixelRatio || 1, 1);
        const cssWidth = canvas.offsetWidth;
        const cssHeight = canvas.offsetHeight;
        canvas.width = cssWidth * ratio;
        canvas.height = cssHeight * ratio;
        canvas.getContext("2d").scale(ratio, ratio);
        signaturePad.clear();
    }
    resizeCanvas();
    window.addEventListener("resize", resizeCanvas);

    document.getElementById('sig-clear-btn').addEventListener('click', () => {
        signaturePad.clear();
    });

    let signatureFile = null;
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

    function dataURLToBlob(dataURL) {
        const parts = dataURL.split(';base64,');
        const contentType = parts[0].split(':')[1];
        const raw = window.atob(parts[1]);
        const rawLength = raw.length;
        const uInt8Array = new Uint8Array(rawLength);
        for (let i = 0; i < rawLength; ++i) {
            uInt8Array[i] = raw.charCodeAt(i);
        }
        return new Blob([uInt8Array], { type: "image/png" });
    }
    // --- 서명 패드 코드 끝 ---

    //  "최종 저장" 버튼 이벤트
    document.getElementById('finalSubmitBtn').addEventListener('click', saveLog);

    //동적 테이블 관리 (HTML onclick을 위해 전역 등록)
    window.removeRow = function(button) {
        button.closest('tr').remove();
    }
    window.addSafetyIssueRow = function(desc = '', action = '', manager = '') {
        const tbody = document.getElementById('safetyIssueTbody');
        const newRow = tbody.insertRow();
        newRow.innerHTML = `
            <td><input type="text" class="issue-desc" placeholder="지적 사항" value="${desc}"></td>
            <td><input type="text" class="issue-action" placeholder="조치 내용" value="${action}"></td>
            <td><input type="text" class="issue-manager" placeholder="담당자" value="${manager}"></td>
            <td><button type="button" class="row-del-btn" onclick="removeRow(this)">삭제</button></td>
        `;
    }

    //  4. 파일 업로드 (공통)
    async function uploadFile(file) {
        const formData = new FormData();
        formData.append('file', file);
        try {
            const response = await fetch('/api/files/upload', {
                method: 'POST',
                body: formData,
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const result = await response.json();
            return result.filePath;
        } catch (error) {
            console.error('File Upload Error:', error);
            alert(`Error: ${file.name} 업로드 중 오류 발생.`);
            throw error;
        }
    }

    //  "최종 저장" 버튼 함수
    async function saveLog() {

        // [필수 항목 검증]
        const logDate = document.getElementById('inspectionDate').value;
        const location = document.getElementById('location').value;
        if (!logDate || !location) {
            alert('오류: 작업일(점검일)과 현장 위치는 필수입니다.');
            return; // 저장 중단
        }

        const submitBtn = document.getElementById('finalSubmitBtn');
        submitBtn.disabled = true;
        submitBtn.textContent = '저장 중... (1/2)';

        // ---  파일 업로드 ---
        const uploadedFilePaths = {
            photos: [],
            attachments: [],
            signature: ""
        };
        try {
            // ( 파일 업로드 로직 )
            const photoFiles = document.getElementById('photosInput').files;
            for (const file of photoFiles) {
                uploadedFilePaths.photos.push(await uploadFile(file));
            }
            const attachmentFiles = document.getElementById('attachmentsInput').files;
            for (const file of attachmentFiles) {
                uploadedFilePaths.attachments.push(await uploadFile(file));
            }
            const sigFileInput = document.getElementById('signatureInput');
            if (sigFileInput.files.length > 0) {
                uploadedFilePaths.signature = await uploadFile(sigFileInput.files[0]);
            } else if (signatureFile) { // (DOM 리스너 내부의 변수)
                uploadedFilePaths.signature = await uploadFile(signatureFile);
            } else if (!signaturePad.isEmpty()) {
                alert("서명 '저장' 버튼을 먼저 눌러주세요.");
                throw new Error("서명 파일 변환 필요");
            }
        } catch (error) {
            alert("파일 업로드에 실패했습니다. 저장을 중단합니다.");
            submitBtn.disabled = false;
            submitBtn.textContent = '안전일지 최종 저장';
            return;
        }

        // ---  JSON 데이터 조립 ---
        submitBtn.textContent = '저장 중... (2/2)';

        const parseListString = (rawString) => {
            if (!rawString) return [];
            return rawString.split(',').map(s => s.trim()).filter(s => s);
        };
        const checklistItems = [];
        document.querySelectorAll('input[name="checklist"]:checked').forEach(chk => {
            checklistItems.push(chk.value);
        });
        const safetyIssues = [];
        document.querySelectorAll('#safetyIssueTbody tr').forEach(row => {
            const description = row.querySelector('.issue-desc').value;
            const action = row.querySelector('.issue-action').value;
            const manager = row.querySelector('.issue-manager').value;
            if (description) {
                safetyIssues.push({ description, action, manager });
            }
        });

        //  최종 JSON 객체 (Java DTO와 Key/타입 일치)
        const logData = {
            company: document.getElementById('company').value,
            logDate: logDate, // (검증한 변수 사용)
            weather: document.getElementById('weather').value,
            location: location, // (검증한 변수 사용)
            author: document.getElementById('author').value,
            manager: document.getElementById('manager').value,
            workType: document.getElementById('workType').value,
            workersCount: parseInt(document.getElementById('workersCount').value) || 0,
            workDetails: document.getElementById('workDetails').value,
            workerNames: parseListString(document.getElementById('workerNames').value),
            equipment: [],
            safetyChecklist: checklistItems.map(itemText => {
                return { item: itemText, status: "Checked" };
            }),
            riskFactors: document.getElementById('riskFactors').value,
            correctiveActions: safetyIssues.map(issue => {
                return `[지적] ${issue.description} / [조치] ${issue.action} / [담당] ${issue.manager}`;
            }).join('\n'),
            remarks: document.getElementById('remarks').value,
            photos: uploadedFilePaths.photos,
            attachments: uploadedFilePaths.attachments,
            signature: uploadedFilePaths.signature
        };


        //  API 전송
        try {
            let url = '/api/safety-log';
            let method = 'POST';

            if (currentEditingId) {
                // '수정' 모드일 경우 (ID가 저장되어 있음)
                url = `/api/safety-log/${currentEditingId}`;
                method = 'PUT'; // 구현 예정
            }

            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(logData),
            });

            if (response.ok) {
                alert(method === 'POST' ? '저장 성공!' : '수정 성공!');
                window.location.href = '/log-list.html'; // 목록으로 이동
            } else {
                const errorText = await response.text();
                alert(` 최종 ${method} 실패: ${errorText} (수정 기능 필요)`);
            }
        } catch (error) {
            console.error('Error:', error);
            alert(' 저장/수정 중 네트워크 오류가 발생했습니다.');
        } finally {
            submitBtn.disabled = false;

        }
    } // saveLog 끝

});


/**
 * 조회
 * (GET) /api/safety-log/{id}
 * '조회/수정' 모드일 때, 1건의 데이터를 조회하여 폼에 채워넣음
 */
async function loadLogDataForEdit(id) {
    console.log(`[조회/수정 모드] ID ${id} 데이터를 불러옵니다.`);

    try {
        const response = await fetch(`/api/safety-log/${id}`);
        if (!response.ok) throw new Error('데이터 조회 실패');
        const dto = await response.json();

        // (폼 채우기)
        document.getElementById('inspectionDate').value = dto.logDate;
        document.getElementById('company').value = dto.company;
        document.getElementById('weather').value = dto.weather;
        document.getElementById('location').value = dto.location;
        document.getElementById('author').value = dto.author;
        document.getElementById('manager').value = dto.manager;
        document.getElementById('workType').value = dto.workType;
        document.getElementById('workersCount').value = dto.workersCount;
        document.getElementById('workDetails').value = dto.workDetails;
        document.getElementById('workerNames').value = (dto.workerNames || []).join(', ');
        document.getElementById('riskFactors').value = dto.riskFactors;
        document.getElementById('remarks').value = dto.remarks;

        // (체크리스트 "보여주기")
        document.querySelectorAll('input[name="checklist"]').forEach(chk => chk.checked = false); // 초기화
        (dto.safetyChecklist || []).forEach(itemDto => {
            const chk = document.querySelector(`input[name="checklist"][value="${itemDto.item}"]`);
            if (chk) chk.checked = true;
        });

        // (지적사항 "보여주기")
        const tbody = document.getElementById('safetyIssueTbody');
        tbody.innerHTML = ''; // 기존 행 삭제
        const issues = (dto.correctiveActions || "").split('\n');
        issues.forEach(line => {
            if (!line) return;
            const desc = line.match(/\[지적\] (.*?) \//)?.[1] || line;
            const action = line.match(/\[조치\] (.*?) \//)?.[1] || '';
            const manager = line.match(/\[담당\] (.*?)$/)?.[1] || '';
            addSafetyIssueRow(desc, action, manager); // 폼에 채워넣음
        });

        // ('수정 모드'로 변경)
        currentEditingId = id;
        document.getElementById('finalSubmitBtn').textContent = `(ID: ${id}) 안전일지 수정하기`;

    } catch (error) {
        alert('데이터를 불러오는 데 실패했습니다: ' + error.message);
        window.location.href = "/log-list.html";
    }
}