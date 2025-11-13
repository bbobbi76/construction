// 현재 수정 중인 ID (null이면 신규 작성)
let currentEditingId = null;

document.addEventListener('DOMContentLoaded', () => {

    // '조회' 모드 확인
    const urlParams = new URLSearchParams(window.location.search);
    const logId = urlParams.get('id');
    if (logId) {
        loadLogDataForEdit(logId);
    }
    // '조회' 모드 확인 끝

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

    let signatureFile = null; // saveLog와 공유할 변수
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
    // 서명 패드 코드 끝

    //  "최종 저장" 버튼 이벤트
    document.getElementById('finalSubmitBtn').addEventListener('click', saveLog);

    //  동적 테이블 관리 (HTML onclick을 위해 전역 등록)
    window.removeRow = function(button) {
        button.closest('tr').remove();
    }
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
            <td><input type="text" class="mat-quantity" placeholder="수량 (예: 5톤)" value="${quantity}"></td>
            <td><button type="button" class="row-del-btn" onclick="removeRow(this)">삭제</button></td>
        `;
    }


    //  파일 업로드 (공통)
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

    // "최종 저장" 버튼 함수
    async function saveLog() {

        // 필수 항목 검증
        const logDate = document.getElementById('logDate').value;
        const location = document.getElementById('location').value;
        if (!logDate || !location) {
            alert('오류: 작업일과 현장 위치는 필수입니다.');
            return; // 저장 중단
        }

        const submitBtn = document.getElementById('finalSubmitBtn');
        submitBtn.disabled = true;
        submitBtn.textContent = '저장 중... (1/2)';

        //  파일 업로드
        const uploadedFilePaths = {
            photos: [],
            attachments: [],
            signature: ""
        };
        try {
            // 파일 업로드 로직
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
            } else if (!signaturePad.isEmpty() && !currentEditingId) { // 수정 모드일 땐 서명 강제 안함
                alert("서명 '저장' 버튼을 먼저 눌러주세요.");
                throw new Error("서명 파일 변환 필요");
            }
        } catch (error) {
            alert("파일 업로드에 실패했습니다. 저장을 중단합니다.");
            submitBtn.disabled = false;
            submitBtn.textContent = currentEditingId ? ` 공사일지 수정하기` : '공사일지 최종 저장';
            return;
        }

        // JSON 데이터 조립
        submitBtn.textContent = '저장 중...';

        const parseListString = (rawString) => {
            if (!rawString) return [];
            return rawString.split(',').map(s => s.trim()).filter(s => s);
        };
        const equipmentList = [];
        document.querySelectorAll('#equipmentTbody tr').forEach(row => {
            const name = row.querySelector('.eq-name').value;
            const count = parseInt(row.querySelector('.eq-count').value) || 0;
            if (name) { equipmentList.push({ name: name, count: count }); }
        });
        const materialList = [];
        document.querySelectorAll('#materialTbody tr').forEach(row => {
            const name = row.querySelector('.mat-name').value;
            const quantity = row.querySelector('.mat-quantity').value;
            if (name) { materialList.push({ name: name, quantity: quantity }); }
        });

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
            remarks: document.getElementById('remarks').value,
            workerNames: parseListString(document.getElementById('workerNames').value),
            equipment: equipmentList,
            materials: materialList,
            photos: uploadedFilePaths.photos,
            attachments: uploadedFilePaths.attachments,
            signature: uploadedFilePaths.signature
        };

        //  API 전송 (분기 처리)
        try {
            let url = '/api/construction-log';
            let method = 'POST';

            if (currentEditingId) {
                url = `/api/construction-log/${currentEditingId}`;
                method = 'PUT'; // 수정 (PUT)
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
                // (수정) 실패 알림창 정리
                alert(`${method === 'POST' ? '저장' : '수정'} 실패: ${errorText}`);
            }
        } catch (error) {
            console.error('Error:', error);
            alert(' 저장/수정 중 네트워크 오류가 발생했습니다.');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = currentEditingId ? `(ID: ${currentEditingId}) 공사일지 수정하기` : '공사일지 최종 저장';
        }
    }


    /**
     * 조회 (GET) /api/construction-log/{id}
     */
    async function loadLogDataForEdit(id) {
        console.log(`[조회/수정 모드] ID ${id} 데이터를 불러옵니다.`);

        try {
            const response = await fetch(`/api/construction-log/${id}`);
            if (!response.ok) throw new Error('데이터 조회 실패');
            const dto = await response.json();

            // (폼 채우기)
            document.getElementById('logDate').value = dto.logDate;
            document.getElementById('company').value = dto.company;
            document.getElementById('weather').value = dto.weather;
            document.getElementById('location').value = dto.location;
            document.getElementById('author').value = dto.author;
            document.getElementById('manager').value = dto.manager;
            document.getElementById('workType').value = dto.workType;
            document.getElementById('workersCount').value = dto.workersCount;
            document.getElementById('workDetails').value = dto.workDetails;
            document.getElementById('workerNames').value = (dto.workerNames || []).join(', ');
            document.getElementById('remarks').value = dto.remarks;

            // TODO: 서명, 사진, 첨부파일 '미리보기' 로직 (현재는 생략됨)

            // (테이블 "보여주기")
            const eqTbody = document.getElementById('equipmentTbody');
            eqTbody.innerHTML = ''; // 기존 행 삭제
            (dto.equipment || []).forEach(eq => {
                addEquipmentRow(eq.name, eq.count);
            });
            const matTbody = document.getElementById('materialTbody');
            matTbody.innerHTML = ''; // 기존 행 삭제
            (dto.materials || []).forEach(mat => {
                addMaterialRow(mat.name, mat.quantity);
            });

            // ('수정 모드'로 변경)
            currentEditingId = id;
            document.getElementById('finalSubmitBtn').textContent = ` 공사일지 수정하기`;

        } catch (error) {
            alert('데이터를 불러오는 데 실패했습니다: ' + error.message);
            window.location.href = "/log-list.html";
        }
    }

});