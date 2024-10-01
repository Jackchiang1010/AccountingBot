// 在網頁加載時執行
document.addEventListener('DOMContentLoaded', function() {
    const checkLineUserId = setInterval(() => {
        if (lineUserId) {
            clearInterval(checkLineUserId); // 清除檢查
            console.log("lineUserId : " + lineUserId);
            fetchAndRenderAlerts();
        }
    }, 100); // 每100毫秒檢查一次

    // 點擊新增時間按鈕時，發送 POST 請求來新增提醒
    document.getElementById('add-alert-btn').addEventListener('click', function() {
        const requestBody = {
            time: "12:00:00",
            description: "該記帳囉",
            lineUserId: lineUserId
        };

        fetch('/api/1.0/alert/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        })
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('新增定時提醒失敗');
            })
            .then(data => {
                console.log("定時提醒新增成功: ", data);
                fetchAndRenderAlerts(); // 重新載入提醒列表
            })
            .catch(error => {
                console.error("發生錯誤: ", error);
            });
    });
});

function fetchAndRenderAlerts() {
    const alertContainer = document.getElementById('alert-container');
    const apiUrl = `/api/1.0/alert/get?lineUserId=${lineUserId}`;

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
            const alerts = data.data;

            alertContainer.innerHTML = '';

            alerts.forEach(alert => {
                const timeInput = document.createElement('input');
                timeInput.type = 'time';
                timeInput.value = alert.time;
                timeInput.className = 'time-input';

                const descriptionInput = document.createElement('input');
                descriptionInput.type = 'text';
                descriptionInput.value = alert.description;
                descriptionInput.className = 'description-input';

                const saveButton = document.createElement('button');
                saveButton.className = 'action-btn';
                saveButton.style.backgroundColor = '#C5F9D7';
                saveButton.textContent = '儲存';

                const deleteButton = document.createElement('button');
                deleteButton.className = 'action-btn';
                deleteButton.style.backgroundColor = '#F27A7D';
                deleteButton.textContent = '刪除';

                const timeNoteRow = document.createElement('div');
                timeNoteRow.className = 'time-note-row';

                const leftSection = document.createElement('div');
                leftSection.className = 'left-section';

                const timeLabel = document.createElement('label');
                timeLabel.textContent = "時間:";
                const descriptionLabel = document.createElement('label');
                descriptionLabel.textContent = "備註:"

                leftSection.appendChild(timeLabel);
                leftSection.appendChild(timeInput);
                leftSection.appendChild(descriptionLabel);
                leftSection.appendChild(descriptionInput);

                const rightSection = document.createElement('div');
                rightSection.className = 'right-section';
                rightSection.appendChild(saveButton);
                rightSection.appendChild(deleteButton);

                timeNoteRow.appendChild(leftSection);
                timeNoteRow.appendChild(rightSection);

                leftSection.dataset.alertId = alert.id;

                saveButton.addEventListener('click', function () {
                    const alertId = leftSection.dataset.alertId;
                    const updatedTime = timeInput.value;
                    const updatedDescription = descriptionInput.value;

                    const requestBody = {
                        id: alertId,
                        time: updatedTime,
                        description: updatedDescription,
                        lineUserId: lineUserId
                    };

                    fetch('/api/1.0/alert/update', {
                        method: 'PUT',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(requestBody)
                    })
                        .then(response => {
                            if (response.ok) {
                                return response.json();
                            }
                            throw new Error('更新定時提醒失敗');
                        })
                        .then(data => {
                            showPopup("定時提醒更新成功");
                            console.log("定時提醒更新成功: ", data);
                            fetchAndRenderAlerts();
                        })
                        .catch(error => {
                            console.error("發生錯誤: ", error);
                        });
                });

                deleteButton.addEventListener('click', function () {
                    const alertId = leftSection.dataset.alertId;

                    const requestBody = {
                        id: alertId
                    };

                    fetch('/api/1.0/alert/delete', {
                        method: 'DELETE',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(requestBody)
                    })
                        .then(response => {
                            if (response.ok) {
                                return response.json();
                            }
                            throw new Error('刪除定時提醒失敗');
                        })
                        .then(data => {
                            showPopup("定時提醒刪除成功");
                            console.log("定時提醒刪除成功: ", data);
                            fetchAndRenderAlerts();
                        })
                        .catch(error => {
                            console.error("發生錯誤: ", error);
                        });
                });

                alertContainer.appendChild(timeNoteRow);
            });
        })
        .catch(error => console.error('Error fetching alert data:', error));
}

function showPopup(message) {
    // 創建彈出視窗的容器
    const popupContainer = document.createElement('div');
    popupContainer.className = 'popup-container';

    // 創建彈出視窗內容
    const popupContent = document.createElement('div');
    popupContent.className = 'popup-content';
    popupContent.textContent = message;

    // 將內容加入彈出視窗容器
    popupContainer.appendChild(popupContent);

    // 將彈出視窗容器加入 body
    document.body.appendChild(popupContainer);

    // 1 秒後自動關閉
    setTimeout(() => {
        if (popupContainer.parentNode) {
            document.body.removeChild(popupContainer);
        }
    }, 1000);

    // 點擊視窗外部時關閉
    popupContainer.addEventListener('click', (event) => {
        if (event.target === popupContainer) {
            document.body.removeChild(popupContainer);
        }
    });
}