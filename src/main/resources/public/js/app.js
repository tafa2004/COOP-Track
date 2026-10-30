// CoopTrack frontend - dashboard and cooperative records
"use strict";

const $ = (id) => document.getElementById(id);
const members = [];
const tasks = [];
const contributions = [];
const supportRecords = [];
const invitations = [];

async function api(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
  });

  const text = await response.text();
  let data = {};
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = { message: text };
    }
  }

  if (!response.ok) {
    throw new Error(data.error || data.message || `HTTP ${response.status}`);
  }
  return data;
}

function safe(value) {
  return String(value ?? "").replace(
    /[&<>"']/g,
    (ch) =>
      ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;",
      })[ch],
  );
}

function notify(message) {
  alert(message);
}

function setCount(id, value) {
  const element = $(id);
  if (element) element.textContent = value;
}

function showSection(name) {
  document.querySelectorAll(".page-section").forEach((section) => {
    section.style.display = section.id === name ? "" : "none";
  });

  document.querySelectorAll(".nav-link").forEach((link) => {
    link.classList.toggle("active", link.getAttribute("href") === `#${name}`);
  });

  const titles = {
    dashboard: "Dashboard",
    members: "Members",
    tasks: "Tasks & Work Days",
    contributions: "Contributions",
    support: "Member Support",
    invitations: "Invitation Keys",
  };

  if ($("page-title"))
    $("page-title").textContent = titles[name] || "Dashboard";
}

function navigate() {
  const name = location.hash.replace("#", "") || "dashboard";
  showSection(name);
  loadSectionData(name);
}

async function loadDashboard() {
  try {
    const [m, t, c, s] = await Promise.all([
      api("/api/members"),
      api("/api/tasks"),
      api("/api/contributions"),
      api("/api/support"),
    ]);

    setCount("member-count", m.length);
    setCount("task-count", t.length);
    setCount(
      "pending-count",
      t.filter(
        (task) =>
          !["completed", "complete", "done"].includes(
            String(task.progress || task.status || "").toLowerCase(),
          ),
      ).length,
    );
    setCount("record-count", c.length);
    setCount("support-count", s.length);
  } catch (error) {
    console.error("Dashboard loading failed:", error);
  }
}

function updateMemberDropdowns() {
  ["task-assignee", "record-member", "support-member"].forEach((id) => {
    const select = $(id);
    if (!select) return;

    const previous = select.value;
    select.innerHTML =
      '<option value="">Select a member</option>' +
      members
        .map(
          (member) =>
            `<option value="${safe(member.id)}">${safe(member.name)}</option>`,
        )
        .join("");

    if (members.some((member) => String(member.id) === previous)) {
      select.value = previous;
    }
  });
}

async function loadMembers() {
  try {
    const data = await api("/api/members");
    if (!Array.isArray(data)) throw new Error("Unexpected members response");

    members.splice(0, members.length, ...data);
    const list = $("member-list");

    if (list) {
      list.innerHTML = members.length
        ? members
            .map(
              (member) => `
          <tr>
            <td>${safe(member.name)}</td>
            <td>${safe(member.email)}</td>
          </tr>`,
            )
            .join("")
        : '<tr><td colspan="2" class="empty">No members added yet.</td></tr>';
    }

    updateMemberDropdowns();
    setCount("member-count", members.length);
  } catch (error) {
    console.error("Loading members failed:", error);
  }
}

async function loadTasks() {
  try {
    const data = await api("/api/tasks");
    if (!Array.isArray(data)) throw new Error("Unexpected tasks response");

    tasks.splice(0, tasks.length, ...data);
    const list = $("task-list");

    if (list) {
      list.innerHTML = tasks.length
        ? tasks
            .map(
              (task) => `
          <tr>
            <td>${safe(task.title)}</td>
            <td>${safe(task.assignee || task.member || task.memberName || "")}</td>
            <td>${safe(task.deadline || "")}</td>
            <td>${safe(task.workDays ?? "")}</td>
            <td>${safe(task.progress || task.status || "Pending")}</td>
          </tr>`,
            )
            .join("")
        : '<tr><td colspan="5" class="empty">No tasks created yet.</td></tr>';
    }

    setCount("task-count", tasks.length);
    setCount(
      "pending-count",
      tasks.filter(
        (task) =>
          !["completed", "complete", "done"].includes(
            String(task.progress || task.status || "").toLowerCase(),
          ),
      ).length,
    );
  } catch (error) {
    console.error("Loading tasks failed:", error);
  }
}

async function loadContributions() {
  try {
    const data = await api("/api/contributions");
    if (!Array.isArray(data))
      throw new Error("Unexpected contributions response");

    contributions.splice(0, contributions.length, ...data);
    const list = $("record-list");

    if (list) {
      list.innerHTML = contributions.length
        ? contributions
            .map(
              (item) => `
          <tr>
            <td>${safe(item.type || "")}</td>
            <td>${safe(item.member || item.memberName || item.memberId || "")}</td>
            <td>${safe(item.description || "")}</td>
            <td>${safe(item.amount ?? "")}</td>
          </tr>`,
            )
            .join("")
        : '<tr><td colspan="4" class="empty">No contributions recorded yet.</td></tr>';
    }

    setCount("record-count", contributions.length);
  } catch (error) {
    console.error("Loading contributions failed:", error);
  }
}

async function loadSupport() {
  try {
    const data = await api("/api/support");
    if (!Array.isArray(data)) throw new Error("Unexpected support response");

    supportRecords.splice(0, supportRecords.length, ...data);
    const list = $("support-list");

    if (list) {
      list.innerHTML = supportRecords.length
        ? supportRecords
            .map(
              (item) => `
          <tr>
            <td>${safe(item.member || item.memberName || item.memberId || "")}</td>
            <td>${safe(item.type || "")}</td>
            <td>${safe(item.description || "")}</td>
            <td>${safe(item.amount ?? "")}</td>
          </tr>`,
            )
            .join("")
        : '<tr><td colspan="4" class="empty">No support records added yet.</td></tr>';
    }

    setCount("support-count", supportRecords.length);
  } catch (error) {
    console.error("Loading support records failed:", error);
  }
}

async function loadInvitations() {
  try {
    const data = await api("/api/invitations");
    if (!Array.isArray(data))
      throw new Error("Unexpected invitations response");

    invitations.splice(0, invitations.length, ...data);
    const list = $("invitation-list");

    if (list) {
      list.innerHTML = invitations.length
        ? invitations
            .map(
              (item) => `
          <tr>
            <td>${safe(item.key || item.invitationKey || "")}</td>
            <td>${safe(item.note || item.purpose || "")}</td>
            <td>${safe(item.status || "")}</td>
          </tr>`,
            )
            .join("")
        : '<tr><td colspan="3" class="empty">No invitation keys available yet.</td></tr>';
    }
  } catch (error) {
    console.error("Loading invitations failed:", error);
  }
}

async function loadSectionData(name) {
  if (name === "dashboard") await loadDashboard();
  if (name === "members") await loadMembers();
  if (name === "tasks") {
    await loadMembers();
    await loadTasks();
  }
  if (name === "contributions") {
    await loadMembers();
    await loadContributions();
  }
  if (name === "support") {
    await loadMembers();
    await loadSupport();
  }
  if (name === "invitations") await loadInvitations();
}

function formDataObject(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function bindForm(id, handler) {
  const form = $(id);
  if (!form) return;

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = form.querySelector('[type="submit"]');
    if (button) button.disabled = true;

    try {
      await handler(formDataObject(form));
      form.reset();
      await Promise.all([
        loadDashboard(),
        loadMembers(),
        loadTasks(),
        loadContributions(),
        loadSupport(),
        loadInvitations(),
      ]);
    } catch (error) {
      console.error(`${id} submission failed:`, error);
      notify(`Could not save record: ${error.message}`);
    } finally {
      if (button) button.disabled = false;
    }
  });
}

bindForm("member-form", async (data) => {
  if (!data.name?.trim() || !data.email?.trim()) {
    throw new Error("Enter the member's name and email.");
  }
  await api("/api/members", {
    method: "POST",
    body: JSON.stringify({
      name: data.name.trim(),
      email: data.email.trim(),
    }),
  });
  notify("Member registered successfully.");
});

bindForm("task-form", async (data) => {
  await api("/api/tasks", {
    method: "POST",
    body: JSON.stringify({
      title: data.title,
      assignee: members.find(member => String(member.id) === String(data.memberId))?.name || "",
      deadline: data.deadline || "",
      workDays: Number(data.workDays || 1),
    }),
  });
  notify("Task saved successfully.");
});

bindForm("record-form", async (data) => {
  await api("/api/contributions", {
    method: "POST",
    body: JSON.stringify({
      member: members.find(member => String(member.id) === String(data.memberId))?.name || "",
      type: data.type,
      description: data.description,
      amount: data.amount || "",
    }),
  });
  notify("Contribution saved successfully.");
});

bindForm("support-form", async (data) => {
  await api("/api/support", {
    method: "POST",
    body: JSON.stringify({
      member: members.find(member => String(member.id) === String(data.memberId))?.name || "",
      type: data.type,
      description: data.description,
      amount: data.amount || "",
    }),
  });
  notify("Support record saved successfully.");
});

bindForm("invitation-form", async (data) => {
  const result = await api("/api/invitations", {
    method: "POST",
    body: JSON.stringify({
      note: data.note || "Cooperative membership",
    }),
  });
  notify(
    `Invitation created.\nKey: ${result.key || "Check the invitation list"}`,
  );
});

document.addEventListener("DOMContentLoaded", async () => {
  document
    .querySelectorAll('.navigation a[href^="#"], .quick-link[href^="#"]')
    .forEach((link) => {
      link.addEventListener("click", () => {
        const name = link.getAttribute("href").slice(1);
        showSection(name);
      });
    });

  window.addEventListener("hashchange", navigate);
  navigate();
  await loadDashboard();
  await loadMembers();
  await loadTasks();
  await loadContributions();
  await loadSupport();
  await loadInvitations();
});


