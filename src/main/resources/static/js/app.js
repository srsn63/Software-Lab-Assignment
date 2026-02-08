function login() {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    fetch("/api/auth/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({username, password})
    })
    .then(res => res.json())
    .then(data => {
        if (data.token) {
            localStorage.setItem("token", data.token);
            redirectBasedOnRole();
        } else {
            document.getElementById("message").innerText = "Login failed";
        }
    });
}

function redirectBasedOnRole() {
    const token = localStorage.getItem("token");
    const payload = JSON.parse(atob(token.split('.')[1]));
    const role = payload.role;

    if (role === "ROLE_STUDENT")
            window.location.href = "/student";
        else if (role === "ROLE_TEACHER")
            window.location.href = "/teacher";
        else window.location.href  = "/login";

}

function logout() {
    localStorage.removeItem("token");
    window.location.href = "/login";
}

function authHeader() {
    return {
        "Authorization": "Bearer " + localStorage.getItem("token"),
        "Content-Type": "application/json"
    };
}

function loadStudentProfile() {
    fetch("/api/students/me", {headers: authHeader()})
        .then(res => res.json())
        .then(data => {
            document.getElementById("studentProfile").innerText =
                "Name: " + data.fullName;
        });

    fetch("/api/courses", {headers: authHeader()})
        .then(res => res.json())
        .then(courses => {
            let list = document.getElementById("courseList");
            list.innerHTML = "";
            courses.forEach(c => {
                let li = document.createElement("li");
                li.innerHTML = c.title +
                    " <button onclick='enroll(" + c.id + ")'>Enroll</button>";
                list.appendChild(li);
            });
        });
}

function enroll(courseId) {
    fetch("/api/students/me/enroll/" + courseId, {
        method: "POST",
        headers: authHeader()
    }).then(() => alert("Enrolled"));
}

function loadTeacherProfile() {
    fetch("/api/teachers/me", {headers: authHeader()})
        .then(res => res.json())
        .then(data => {
            document.getElementById("teacherProfile").innerText =
                "Name: " + data.fullName;
        });

    fetch("/api/teachers/me/courses", {headers: authHeader()})
        .then(res => res.json())
        .then(courses => {
            let list = document.getElementById("teacherCourses");
            list.innerHTML = "";
            courses.forEach(c => {
                let li = document.createElement("li");
                li.innerText = c.title;
                list.appendChild(li);
            });
        });
}

function createCourse() {
    const title = document.getElementById("courseTitle").value;
    const description = document.getElementById("courseDesc").value;

    fetch("/api/teachers/me/courses", {
        method: "POST",
        headers: authHeader(),
        body: JSON.stringify({title, description})
    }).then(() => {
        alert("Course Created");
        loadTeacherProfile();
    });
}
