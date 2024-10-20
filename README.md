# 吉包帳
A bookkeeping bot that combines the Taiwanese dialect pun of "wallet" and the meaning of "treasure."
It symbolizes discovering the treasures of financial management during the process of bookkeeping, making it easy to track every expense and income.

# About

This accounting bot offers the following features to help users manage their finances more effectively:

- **Record Income and Expenses** : Quickly and conveniently record daily income and expenses, with category management.
- **Financial Calendar** : View monthly financial status clearly through a calendar view.

Additionally, it provides various charts and reports for in-depth financial analysis:

- **Income and Expense Charts** : Visual representation of income and expense data.
- **Balance Charts** : Easily track the balance between income and expenses through charts.
- **Financial Advice and Analysis** : Offers intelligent financial suggestions based on the user's financial situation, helping to optimize financial management.
- **Export Last Month’s Report** : Generate and export detailed income and expense reports for the previous month.

In addition, it includes thoughtful reminder features:

- **Budget Alerts** : Automatically sends reminders when expenses exceed the set budget.
- **Scheduled Reminders** : Set scheduled notifications to remind users to record their transactions.


# Built with

**Bot** : ![LINE](https://img.shields.io/badge/LINE-00C300.svg?style=for-the-badge&logo=LINE&logoColor=white)

**Back-End** :  ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F.svg?style=for-the-badge&logo=Spring-Boot&logoColor=white), ![Amazon EC2](https://img.shields.io/badge/Amazon%20EC2-FF9900.svg?style=for-the-badge&logo=Amazon-EC2&logoColor=white)

**Database** :  ![RDS MySQL](https://img.shields.io/badge/Amazon%20RDS-527FFF.svg?style=for-the-badge&logo=Amazon-RDS&logoColor=white), ![MySQL](https://img.shields.io/badge/MySQL-4479A1.svg?style=for-the-badge&logo=MySQL&logoColor=white)

**Front-End** : ![HTML5](https://img.shields.io/badge/HTML5-E34F26.svg?style=for-the-badge&logo=HTML5&logoColor=white), ![CSS](https://img.shields.io/badge/CSS3-1572B6.svg?style=for-the-badge&logo=CSS3&logoColor=white), ![Java Script](https://img.shields.io/badge/JavaScript-F7DF1E.svg?style=for-the-badge&logo=JavaScript&logoColor=black)

**Load Balance** : ![Elastic Load balancer](https://img.shields.io/badge/AWS%20Elastic%20Load%20Balancing-8C4FFF.svg?style=for-the-badge&logo=AWS-Elastic-Load-Balancing&logoColor=white)

**Storage** : ![S3](https://img.shields.io/badge/Amazon%20S3-569A31.svg?style=for-the-badge&logo=Amazon-S3&logoColor=white)

**Cache** :  ![ElastiCache](https://img.shields.io/badge/Amazon%20ElastiCache-C925D1.svg?style=for-the-badge&logo=Amazon-ElastiCache&logoColor=white), ![Redis](https://img.shields.io/badge/Redis-FF4438.svg?style=for-the-badge&logo=Redis&logoColor=white)


**Version Control** : ![Git](https://img.shields.io/badge/Git-F05032.svg?style=for-the-badge&logo=Git&logoColor=white)

**CI/CD** :  ![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF.svg?style=for-the-badge&logo=GitHub-Actions&logoColor=white)

# Achitecture

![Achitecture](https://accountingbot.s3.ap-northeast-1.amazonaws.com/CV/%E6%9E%B6%E6%A7%8B%E5%9C%962.png)

- Developed and deployed using **Java Spring Boot** on **EC2**
- Traffic distribution managed by **Elastic Load Balancer**
- Incomplete accounting information temporarily stored in **ElastiCache** **Redis**
- Data stored in an **RDS** **MySQL** database
- Integrated with OpenAI API to provide financial analysis and recommendations based on user activity
- Automated deployment through **GitHub Actions**

# DEMO

[![Watch the video](https://img.youtube.com/vi/MKKhimUhJNs/0.jpg)](https://www.youtube.com/watch?v=MKKhimUhJNs)

# Contact

[![Linkedin](https://img.shields.io/badge/LinkedIn-0A66C2.svg?style=for-the-badge&logo=LinkedIn&logoColor=white)](https://www.linkedin.com/in/%E7%85%92%E5%BD%AC-%E6%B1%9F-b0b249307/)
[![Gmail](https://img.shields.io/badge/Gmail-EA4335.svg?style=for-the-badge&logo=Gmail&logoColor=white)](mailto:jackgg44109@gmail.com)