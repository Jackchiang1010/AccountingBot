// 在網頁加載時執行
document.addEventListener('DOMContentLoaded', function() {
    const checkLineUserId = setInterval(() => {
        if (lineUserId) {
            clearInterval(checkLineUserId);
            console.log("lineUserId : " + lineUserId);

            // updateCalendar();
            initializeFullCalendar();
        }
    }, 100);

    const urlParams = new URLSearchParams(window.location.search);
    type = urlParams.get('type');
});

function initializeFullCalendar() {
    const calendarEl = document.getElementById('calendar');
    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        showNonCurrentDates: false,
        locale: 'zh-tw',
        height: 600, // 設定固定高度
        contentHeight: 500, // 設定內容區域高度
        dayMaxEventRows: true, // 每天最多顯示的事件行數，超出後會顯示 "more"
        dayMaxEvents: 3, // 每天最多顯示的事件數量，超出後會顯示 "more"
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: ''
        },
        dateClick: function(info) {
            // 檢查點擊的日期是否在目前顯示的月份內
            const clickedDate = new Date(info.dateStr);
            const currentStartMonth = calendar.view.currentStart.getMonth(); // 取得日曆當前顯示月份的起始月份

            console.log("clickedDate : " + clickedDate);
            console.log("currentStartMonth : " + currentStartMonth);
            console.log("clickedDate.getMonth() : " + clickedDate.getMonth());


            if (clickedDate.getMonth() !== currentStartMonth) {
                // 如果點擊的日期不在當前月份，則直接返回，不執行任何操作
                console.log("return");
                return;
            }

            console.log("info.dateStr : " + info.dateStr);
            // 如果是當月的日期，載入該日期的記帳資料
            loadExpensesForDay(info.dateStr);
        },
        dayCellClassNames: function(arg) {
            const currentStartMonth = calendar.view.currentStart.getMonth(); // 取得日曆目前顯示月份的起始月份
            const cellMonth = arg.date.getMonth(); // 取得當前單元格的月份

            // 如果該單元格的月份不是本月，則加上 'hidden-day' 類別
            if (currentStartMonth !== cellMonth) {
                return ['hidden-day'];
            }
            return [];
        },
        events: function(fetchInfo, successCallback, failureCallback) {
            const startDate = fetchInfo.startStr;
            const endDate = fetchInfo.endStr;
            fetchTransactions(startDate, endDate, lineUserId)
                .then(transactions => {
                    const events = transactions.map(tx => ({
                        title: tx.description,
                        start: tx.date,
                        color: tx.type === 0 ? '#C5F9D7' : '#F27A7D' // 根據收入或支出設定顏色
                    }));
                    successCallback(events);
                })
                .catch(error => {
                    console.error(error);
                    alert('載入記帳資料失敗，請稍後再試。');
                    failureCallback(error);
                });
        }
    });
    calendar.render();
}

function fetchTransactions(startDate, endDate, lineUserId) {
    return fetch(`/api/1.0/transaction/get/details?startDate=${startDate}&endDate=${endDate}&lineUserId=${lineUserId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('網路錯誤：無法獲取記帳資料');
            }
            return response.json(); // 返回 JSON 格式的資料
        });
}

function loadExpensesForDay(day) {
    const clickedDate = new Date(day);
    const year = clickedDate.getFullYear();
    const month = clickedDate.getMonth() + 1;
    const date = ('0' + clickedDate.getDate()).slice(-2);
    const targetDate = `${year}-${('0' + month).slice(-2)}-${date}`;

    console.log("targetDate : " + targetDate);

    fetch(`/api/1.0/transaction/get/details?startDate=${targetDate}&endDate=${targetDate}&lineUserId=${lineUserId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('網路錯誤：無法獲取記帳資料');
            }
            return response.json();
        })
        .then(data => {
            const modalContent = document.getElementById('modal-content');
            modalContent.innerHTML = '';

            if (data.length === 0) {
                const noDataMessage = document.createElement('p');
                noDataMessage.textContent = `在 ${targetDate} 沒有記帳資料。`;
                modalContent.appendChild(noDataMessage);

                const goToRecordBtn = document.createElement('button');
                goToRecordBtn.textContent = '去記帳';
                goToRecordBtn.onclick = () => {
                    window.location.href = `/record.html?date=${targetDate}`;
                };
                modalContent.appendChild(goToRecordBtn);
            } else {
                const detailsList = document.createElement('ul');
                data.forEach(expense => {
                    const listItem = document.createElement('li');
                    const typeText = expense.type === 0 ? '收入' : '支出';
                    listItem.innerHTML  = `${expense.date} ${typeText} <br> ${expense.description} - $${expense.cost}`;

                    const viewDetailsBtn = document.createElement('button');
                    viewDetailsBtn.textContent = '編輯';
                    viewDetailsBtn.onclick = () => {
                        window.location.href = `transactionDetail.html?lineUserId=${lineUserId}&transactionId=${expense.id}`;
                    };

                    listItem.appendChild(viewDetailsBtn);
                    detailsList.appendChild(listItem);
                });
                modalContent.appendChild(detailsList);

                const goToRecordBtn = document.createElement('button');
                goToRecordBtn.textContent = '去記帳';
                goToRecordBtn.onclick = () => {
                    window.location.href = `/record.html?date=${targetDate}`;
                };
                modalContent.appendChild(goToRecordBtn);
            }

            // Open the modal
            document.getElementById('modal').style.display = 'block';
        })
        .catch(error => {
            console.error(error);
            const modalContent = document.getElementById('modal-content');
            modalContent.textContent = '載入記帳資料失敗，請稍後再試。';
            document.getElementById('modal').style.display = 'block';
        });
}

// Close the modal when clicking on the close button or outside of the modal
window.onclick = function(event) {
    const modal = document.getElementById('modal');
    if (event.target == modal) {
        modal.style.display = 'none';
    }
}

