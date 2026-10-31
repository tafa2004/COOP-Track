CoopTrack

Cooperative Task & Contribution Management System

Better Records. Better Accountability. Better Cooperative Management.

CoopTrack is a Java-based cooperative management system designed to help agricultural cooperatives organise members, manage tasks, track contributions and maintain records of financial and non-financial support.

The system aims to replace scattered communication and paper-based record keeping with a centralised digital platform that promotes accountability, transparency and efficient cooperative management.

---

1. Project Overview

Agricultural cooperatives manage several activities, including member registration, task allocation, work-day tracking, member contributions and the distribution of support.

Managing these activities manually can make it difficult to maintain accurate records and monitor cooperative operations.

CoopTrack provides a digital solution for organising these activities and improving access to important cooperative information.

2. Problem Statement

Many agricultural cooperatives depend on manual record books and informal communication to manage their operations.

This can lead to:

- Loss of important member information.
- Difficulty tracking assigned tasks and completed activities.
- Poor monitoring of members' work days.
- Inaccurate or incomplete contribution records.
- Difficulty tracking financial and material support.
- Delays in retrieving cooperative information.

CoopTrack aims to address these challenges through a centralised management system.

3. Project Objectives

The project aims to:

1. Maintain organised records of cooperative members.
2. Assign tasks and monitor their progress and deadlines.
3. Record and manage member contributions.
4. Track financial and non-financial support provided to members.
5. Improve accountability and accessibility of cooperative records.
6. Apply Object-Oriented Programming principles in developing a practical software solution.

4. Core Features

Feature| Description
Member Management| Maintain and organise cooperative member information.
Task Management| Create and assign tasks, record deadlines and monitor progress.
Work-Day Tracking| Record the working days associated with assigned tasks.
Contribution Management| Maintain records of member contributions.
Support Management| Record financial and non-financial support provided to members.
Invitation-Key Management| Restrict registration through administrator-issued invitation keys.

5. Technologies Used

The project uses the following technologies:

- Java – Backend development and application logic.
- Apache Maven – Dependency and build management.
- Javalin – Java web framework for HTTP routes and API development.
- SQLite – Database management and data persistence.
- HTML – Frontend structure.
- CSS – User interface styling.
- JavaScript – Frontend functionality and interaction.
- Git and GitHub – Version control and team collaboration.

6. Object-Oriented Programming Concepts

CoopTrack applies the following Object-Oriented Programming concepts:

- Classes and Objects: Represent cooperative entities and system operations.
- Encapsulation: Protect data through private attributes and controlled access using getters and setters.
- Inheritance: Promote code reuse through class hierarchies where appropriate.
- Polymorphism: Allow related objects to implement specialised behaviours.
- Abstraction: Represent essential behaviours while hiding implementation details.
- Exception Handling: Handle invalid inputs and application errors.
- Collections: Organise and manage groups of application objects.

7. Project Structure

The project follows the standard Maven directory structure.

COOP-Track/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── cooptrack/
│       │           ├── Main.java
│       │           ├── Admin.java
│       │           ├── Task.java
│       │           ├── Record.java
│       │           ├── ContributionRecord.java
│       │           ├── SupportRecord.java
│       │           └── InvalidKeyException.java
│       │
│       └── resources/
│           └── public/
│               ├── index.html
│               ├── css/
│               └── js/
│
├── pom.xml
└── README.md

Note: The project structure may evolve as additional classes and components are developed and integrated.

8. Getting Started

Prerequisites

Ensure that the following are installed:

- Java Development Kit (JDK).
- Apache Maven.
- Git.
- Visual Studio Code or another Java-compatible IDE.

Clone the Repository

git clone https://github.com/tafa2004/COOP-Track.git

Navigate to the Project Directory

cd COOP-Track

Compile the Project

mvn clean compile

Run the Application

Use the configured Maven startup command or the application's documented startup procedure.

The application is intended to run locally at:

http://localhost:7070

9. Team Collaboration

Development takes place through the official GitHub repository:

Repository: https://github.com/tafa2004/COOP-Track

The "main" branch serves as the official project branch.

All contributors are expected to:

1. Clone the official repository.
2. Create individual feature branches.
3. Work on their assigned functionalities.
4. Test their implementations.
5. Commit changes using meaningful commit messages.
6. Push their feature branches to GitHub.
7. Submit Pull Requests for review before merging into "main".

Direct commits to the "main" branch should be avoided.

10. Project Scope

CoopTrack focuses on the management of agricultural cooperative activities, including:

- Member information.
- Task allocation and monitoring.
- Work-day records.
- Member contributions.
- Financial and non-financial member support.
- Administrator-controlled registration.

The project is developed within the requirements of an Object-Oriented Programming course while maintaining a structure that can accommodate future improvements.

11. Future Improvements

Potential improvements include:

- Advanced reporting and analytics.
- Data export functionality.
- Enhanced security and access control.
- Improved user experience.
- Deployment for use by real agricultural cooperatives.
- Additional cooperative management features.

12. Contributors

CoopTrack is being developed by a team of five students as part of an academic Object-Oriented Programming project.

Team Members

1. To be added.
2. To be added.
3. To be added.
4. To be added.
5. To be added.

---

CoopTrack — Better Records. Better Accountability. Better Cooperative Management.
