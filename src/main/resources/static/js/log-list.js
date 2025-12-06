// log-list.js

/**
 * [초기화]
 * DOM이 로드되면 공사일지와 안전일지 목록을 각각 불러옵니다.
 */
document.addEventListener('DOMContentLoaded', () => {
    loadConstructionLogs();
    loadSafetyLogs();
});

// =================================================================================
//                                1. 데이터 로딩 (Read)
// =================================================================================

/**
 * [공사일지 목록 로딩]
 * - 서버에서 공사일지 데이터를 가져와 화면에 렌더링합니다.
 * - cache: 'no-store' 옵션을 사용하여 항상 최신 데이터를 받아옵니다.
 */
function loadConstructionLogs() {
    const listDiv = document.getElementById('constructionList');

    // 로딩 중 표시
    listDiv.innerHTML = '<p style="color:#666; padding:10px;">데이터를 불러오는 중입니다...</p>';

    fetch('/api/construction-log', { cache: 'no-store' })
        .then(response => {
            if (!response.ok) throw new Error("서버 응답 없음");
            return response.json();
        })
        .then(data => {
            // 기존 로딩 문구 제거 후 카드 리스트 추가
            listDiv.innerHTML = '';
            listDiv.appendChild(createCardListHtml(data, 'construction'));
        })
        .catch(error => {
            console.error(error);
            listDiv.innerHTML = '<p style="color:red; padding:10px;">로딩 실패 (서버 연결 필요)</p>';
        });
}

/**
 * [안전일지 목록 로딩]
 * - 서버에서 안전일지 데이터를 가져와 화면에 렌더링합니다.
 */
function loadSafetyLogs() {
    const listDiv = document.getElementById('safetyList');

    // 로딩 중 표시
    listDiv.innerHTML = '<p style="color:#666; padding:10px;">데이터를 불러오는 중입니다...</p>';

    fetch('/api/safety-log', { cache: 'no-store' })
        .then(response => {
            if (!response.ok) throw new Error("서버 응답 없음");
            return response.json();
        })
        .then(data => {
            // 기존 로딩 문구 제거 후 카드 리스트 추가
            listDiv.innerHTML = '';
            listDiv.appendChild(createCardListHtml(data, 'safety'));
        })
        .catch(error => {
            console.error(error);
            listDiv.innerHTML = '<p style="color:red; padding:10px;">로딩 실패 (서버 연결 필요)</p>';
        });
}

// =================================================================================
//                                2. 삭제 기능 (Delete)
// =================================================================================

/**
 * [공사일지 삭제]
 * - 사용자 확인 후 DELETE 요청을 보냅니다.
 * - 성공 시 목록을 새로고침합니다.
 */
async function deleteConstructionLog(id) {
    if (!confirm('정말 이 일지를 삭제하시겠습니까?')) return;

    try {
        const response = await fetch(`/api/construction-log/${id}`, { method: 'DELETE' });

        if (response.ok) {
            alert('삭제되었습니다.');
            loadConstructionLogs(); // 목록 갱신
        } else {
            alert(`삭제 실패: ${await response.text()}`);
        }
    } catch (error) {
        alert('삭제 중 오류가 발생했습니다.');
    }
}

/**
 * [안전일지 삭제]
 * - 사용자 확인 후 DELETE 요청을 보냅니다.
 * - 성공 시 목록을 새로고침합니다.
 */
async function deleteSafetyLog(id) {
    if (!confirm('정말 이 일지를 삭제하시겠습니까?')) return;

    try {
        const response = await fetch(`/api/safety-log/${id}`, { method: 'DELETE' });

        if (response.ok) {
            alert('삭제되었습니다.');
            loadSafetyLogs(); // 목록 갱신
        } else {
            alert(`삭제 실패: ${await response.text()}`);
        }
    } catch (error) {
        alert('삭제 중 오류가 발생했습니다.');
    }
}

// =================================================================================
//                                3. UI 렌더링 (View)
// =================================================================================

/**
 * [카드 리스트 HTML 생성]
 * - 데이터를 받아서 HTML 카드 형태의 리스트(DOM Element)를 반환합니다.
 * - 공사일지와 안전일지 모두 이 함수를 재사용합니다.
 *  @param {Array} data - 일지 데이터 배열
 * @param {String} type - 'construction' 또는 'safety'
 * @returns {HTMLElement} 생성된 리스트 컨테이너
 */
function createCardListHtml(data, type) {
    const listContainer = document.createElement('div');
    listContainer.className = 'log-card-list'; // CSS Grid 스타일 적용

    // 1. 데이터가 없을 경우 처리
    if (!data || data.length === 0) {
        listContainer.innerHTML = '<div style="padding:20px; color:#999; text-align:center; background:#fff; border-radius:8px; border:1px solid #eee;">작성된 일지가 없습니다.</div>';
        return listContainer;
    }

    // 2. 타입별 설정 (API 경로, 아이콘, 이름)
    const isConstruction = type === 'construction';
    const apiPath = isConstruction ? 'construction-log' : 'safety-log';
    const icon = isConstruction ? '🏗️' : '🛡️';
    const typeName = isConstruction ? '공사일지' : '안전일지';

    // 3. 최신순 정렬 (날짜 기준 내림차순)
    data.sort((a, b) => {
        if (a.logDate && b.logDate) {
            return b.logDate.localeCompare(a.logDate);
        }
        return 0;
    });

    // 4. 카드 요소 생성 및 추가
    data.forEach(log => {
        const card = document.createElement('div');
        card.className = 'log-card'; // common.css 스타일 적용

        // 링크 URL 설정
        const viewUrl = `/${isConstruction ? 'construction' : 'safety'}-log.html?id=${log.id}`;
        const pdfUrl = `/api/${apiPath}/${log.id}/pdf`;

        // 삭제 함수 이름 결정 (문자열로 onclick에 넣기 위함)
        const deleteFuncName = isConstruction ? 'deleteConstructionLog' : 'deleteSafetyLog';

        // 카드 내부 HTML 구성
        card.innerHTML = `
            <a href="${viewUrl}" class="info-link">
                <div class="location">
                    <span style="margin-right:8px; font-size:1.2em;">${icon}</span>
                    ${log.location || '현장 위치 없음'}
                </div>
                <div class="meta">
                    <span style="display:flex; align-items:center; gap:5px;">
                         📅 ${log.logDate || '날짜 미상'}
                    </span>
                    <span style="display:flex; align-items:center; gap:5px;">
                         👤 ${log.author || '작성자 미상'}
                    </span>
                    <span style="color:#aaa;">| ${typeName}</span>
                </div>
            </a>
            
            <div class="actions">
                <a href="${pdfUrl}" class="btn-pdf" title="PDF 다운로드">📄 PDF</a>
                <a href="${viewUrl}" class="btn-edit">수정</a>
                <button type="button" class="btn-delete" onclick="${deleteFuncName}(${log.id})">삭제</button>
            </div>
        `;
        listContainer.appendChild(card);
    });

    return listContainer;
}