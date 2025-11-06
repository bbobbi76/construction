
document.addEventListener('DOMContentLoaded', () => {
    loadConstructionLogs();
    loadSafetyLogs();
});

//  공사일지 (Construction)
function loadConstructionLogs() {
    const listDiv = document.getElementById('constructionList');
    listDiv.innerHTML = '<p>로딩 중...</p>';
    fetch('/api/construction-log', { cache: 'no-store' })
        .then(response => response.json())
        .then(data => {
            listDiv.innerHTML = '';
            listDiv.appendChild(createCardListHtml(data, 'construction'));
        })
        .catch(error => listDiv.innerHTML = '<p>공사일지 로딩 실패</p>');
}

// (viewConstructionLog 함수는  createCardListHtml 안에서 직접 링크 생성)

async function deleteConstructionLog(id) {
    if (!confirm(`[공사일지 ID: ${id}] 정말 삭제하시겠습니까?`)) return;

    // 삭제 기능이 아직 없으므로, alert만 띄움
    alert('삭제 실패 (백엔드  기능 필요)');

    // const response = await fetch(`/api/construction-log/${id}`, { method: 'DELETE' });
    // if (response.ok) {
    //     loadConstructionLogs();
    // } else {
    //     alert('삭제 실패 (백엔드 기능 필요)');
    // }
}

// 안전일지 (Safety)
function loadSafetyLogs() {
    const listDiv = document.getElementById('safetyList');
    listDiv.innerHTML = '<p>로딩 중...</p>';
    fetch('/api/safety-log', { cache: 'no-store' })
        .then(response => response.json())
        .then(data => {
            listDiv.innerHTML = '';
            listDiv.appendChild(createCardListHtml(data, 'safety'));
        })
        .catch(error => listDiv.innerHTML = '<p>안전일지 로딩 실패</p>');
}

// (viewSafetyLog 함수는 이제 createCardListHtml 안에서 직접 링크 생성)

async function deleteSafetyLog(id) {
    if (!confirm(`[안전일지 ID: ${id}] 정말 삭제하시겠습니까?`)) return;

    // (삭제 기능이 없으므로, alert만 띄움)
    alert('삭제 실패 (백엔드 5주차 기능 필요)');

    // const response = await fetch(`/api/safety-log/${id}`, { method: 'DELETE' });
    // if (response.ok) {
    //     loadSafetyLogs();
    // } else {
    //     alert('삭제 실패 (삭제 기능 필요)');
    // }
}


//  '카드' HTML 생성
function createCardListHtml(data, type) {
    const listContainer = document.createElement('div');
    listContainer.className = 'log-card-list';

    if (!data || data.length === 0) {
        listContainer.innerHTML = '<p>데이터가 없습니다.</p>';
        return listContainer;
    }

    const isConstruction = type === 'construction';

    // 최신순으로 정렬
    data.sort((a, b) => b.logDate.localeCompare(a.logDate));

    data.forEach(log => {
        const card = document.createElement('div');
        card.className = 'log-card';

        // '그때 쓴 폼'으로 가는 링크 생성
        const viewUrl = `/${isConstruction ? 'construction' : 'safety'}-log.html?id=${log.id}`;

        // 목록에서 카드(작성한 일지) 누르면 들어감
        card.innerHTML = `
            <a href="${viewUrl}" class="info-link">
                <div class="location">${log.location || '현장 위치 없음'}</div>
                <div class="meta">
                    <span>${log.logDate || '날짜 없음'}</span> | 
                    <span>${log.author || '작성자 없음'}</span>
                </div>
            </a>
            
            <div class="actions">
                <a href="${viewUrl}" class="btn-edit">수정</a>
                
                <button type="button" class="btn-delete" onclick="${isConstruction ? 'deleteConstructionLog' : 'deleteSafetyLog'}(${log.id})">삭제</button>
            </div>
        `;
        listContainer.appendChild(card);
    });

    return listContainer;
}