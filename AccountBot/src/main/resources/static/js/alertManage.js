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
                saveButton.textContent = '儲存';

                const deleteButton = document.createElement('button');
                deleteButton.className = 'action-btn';
                deleteButton.textContent = '刪除';

                const timeNoteRow = document.createElement('div');
                timeNoteRow.className = 'time-note-row';
                timeNoteRow.appendChild(timeInput);
                timeNoteRow.appendChild(descriptionInput);
                timeNoteRow.appendChild(saveButton);
                timeNoteRow.appendChild(deleteButton);

                timeNoteRow.dataset.alertId = alert.id;

                saveButton.addEventListener('click', function () {
                    const alertId = timeNoteRow.dataset.alertId;
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
                            console.log("定時提醒更新成功: ", data);
                            alert("定時提醒更新成功");
                            fetchAndRenderAlerts();
                        })
                        .catch(error => {
                            console.error("發生錯誤: ", error);
                            alert("定時提醒更新失敗");
                        });
                });

                deleteButton.addEventListener('click', function () {
                    const alertId = timeNoteRow.dataset.alertId;

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
