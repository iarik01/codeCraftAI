const authApiUrl = "http://localhost:8081/api/auth";
const tasksApiUrl = "http://localhost:8082/api/tasks";
const groupsApiUrl = "http://localhost:8082/api/groups";
const studentTasksApiUrl = "http://localhost:8082/api/student/tasks";
const studentGroupsApiUrl = "http://localhost:8082/api/student/groups";
const submissionsApiUrl = "http://localhost:8082/api/submissions";
const tokenKey = "codecrafters.accessToken";

const state = {
  accessToken: localStorage.getItem(tokenKey),
  authMode: "login",
  currentUser: null,
  teacherView: "generator",
  studentView: "tasks",
  teacherTasks: [],
  groups: [],
  selectedTask: null,
  selectedGroup: null,
  selectedStudentTask: null,
  submissions: [],
};

const el = {
  authView: document.querySelector("#auth-view"),
  dashboardView: document.querySelector("#dashboard-view"),
  loginTab: document.querySelector("#login-tab"),
  registerTab: document.querySelector("#register-tab"),
  loginForm: document.querySelector("#login-form"),
  registerForm: document.querySelector("#register-form"),
  loginSubmit: document.querySelector("#login-submit"),
  registerSubmit: document.querySelector("#register-submit"),
  authStatus: document.querySelector("#auth-status"),
  dashboardSubtitle: document.querySelector("#dashboard-subtitle"),
  currentUserName: document.querySelector("#current-user-name"),
  currentUserEmail: document.querySelector("#current-user-email"),
  currentUserRole: document.querySelector("#current-user-role"),
  logoutButton: document.querySelector("#logout-button"),
  dashboardNav: document.querySelector("#dashboard-nav"),
  content: document.querySelector("#view-content"),
};

bindAuthEvents();
restoreSession();

function bindAuthEvents() {
  el.loginTab.addEventListener("click", () => renderAuthView("login"));
  el.registerTab.addEventListener("click", () => renderAuthView("register"));
  el.logoutButton.addEventListener("click", () => clearSession());

  el.loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await authenticate("login", {
      email: el.loginForm.email.value.trim(),
      password: el.loginForm.password.value,
    });
  });

  el.registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await authenticate("register", {
      name: el.registerForm.name.value.trim(),
      email: el.registerForm.email.value.trim(),
      password: el.registerForm.password.value,
      role: el.registerForm.role.value,
    });
  });
}

async function restoreSession() {
  if (!state.accessToken) {
    renderAuthView();
    return;
  }

  setStatus(el.authStatus, "Восстанавливаем сессию...", "loading");
  try {
    state.currentUser = await request(`${authApiUrl}/me`);
    renderDashboard();
  } catch (error) {
    clearSession("Сессия истекла. Войдите снова.");
  }
}

async function authenticate(action, payload) {
  const button = action === "login" ? el.loginSubmit : el.registerSubmit;
  const defaultText = action === "login" ? "Войти" : "Зарегистрироваться";
  button.disabled = true;
  button.textContent = action === "login" ? "Входим..." : "Создаём аккаунт...";
  setStatus(el.authStatus, "Отправляем данные...", "loading");

  try {
    const data = await request(`${authApiUrl}/${action}`, {
      method: "POST",
      body: JSON.stringify(payload),
      skipAuth: true,
    });
    state.accessToken = data.accessToken;
    localStorage.setItem(tokenKey, state.accessToken);
    state.currentUser = await request(`${authApiUrl}/me`);
    setStatus(el.authStatus, "", "");
    renderDashboard();
  } catch (error) {
    setStatus(el.authStatus, error.message, "error");
  } finally {
    button.disabled = false;
    button.textContent = defaultText;
  }
}

async function request(url, options = {}) {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (state.accessToken && !options.skipAuth) {
    headers.Authorization = `Bearer ${state.accessToken}`;
  }

  const response = await fetch(url, { ...options, headers });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    if (response.status === 401) {
      clearSession("Сессия истекла. Войдите снова.");
    }
    throw new Error(data.message || "Запрос завершился ошибкой");
  }
  return data;
}

function renderAuthView(mode = state.authMode) {
  state.authMode = mode;
  el.authView.classList.remove("hidden");
  el.dashboardView.classList.add("hidden");
  el.loginForm.classList.toggle("hidden", mode !== "login");
  el.registerForm.classList.toggle("hidden", mode !== "register");
  el.loginTab.classList.toggle("active", mode === "login");
  el.registerTab.classList.toggle("active", mode === "register");
}

function renderDashboard() {
  el.authView.classList.add("hidden");
  el.dashboardView.classList.remove("hidden");
  el.currentUserName.textContent = state.currentUser?.name || "Пользователь";
  el.currentUserEmail.textContent = state.currentUser?.email || "";
  el.currentUserRole.textContent = state.currentUser?.role || "";

  if (state.currentUser?.role === "STUDENT") {
    renderStudentDashboard();
  } else {
    renderTeacherDashboard();
  }
}

function renderTeacherDashboard(view = state.teacherView) {
  state.teacherView = view;
  el.dashboardSubtitle.textContent = "Кабинет преподавателя";
  renderNav([
    ["generator", "Генерация"],
    ["tasks", "Мои задания"],
    ["groups", "Группы"],
    ["review", "Проверка решений"],
  ], view, renderTeacherDashboard);

  if (view === "tasks") renderTeacherTasksView();
  else if (view === "groups") renderGroupsView();
  else if (view === "review") renderReviewView();
  else renderGeneratorView();
}

function renderStudentDashboard(view = state.studentView) {
  state.studentView = view;
  el.dashboardSubtitle.textContent = "Кабинет ученика";
  renderNav([
    ["tasks", "Мои задания"],
    ["groups", "Мои группы"],
  ], view, renderStudentDashboard);

  if (view === "groups") renderStudentGroupsView();
  else renderStudentTasksView();
}

function renderNav(items, active, onClick) {
  el.dashboardNav.innerHTML = "";
  items.forEach(([id, label]) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `nav-button${id === active ? " active" : ""}`;
    button.textContent = label;
    button.addEventListener("click", () => onClick(id));
    el.dashboardNav.append(button);
  });
}

function renderGeneratorView() {
  el.content.innerHTML = `
    <div class="view-header"><div><h2>Генерация задания</h2><p>Форма слева, результат генерации справа.</p></div></div>
    <div class="grid-2">
      <section class="card">
        <h2>Параметры</h2>
        <form id="task-form">
          <div class="form-grid">
            <label>Направление <select name="subjectArea"><option value="SCRATCH">Scratch</option><option value="PYTHON" selected>Python</option><option value="ALGORITHMS">Алгоритмика</option><option value="HTML_CSS">HTML/CSS</option></select></label>
            <label>Сложность <select name="difficulty"><option value="BEGINNER" selected>Начальный</option><option value="INTERMEDIATE">Средний</option><option value="ADVANCED">Продвинутый</option></select></label>
            <label>Возраст <select name="gradeLevel"><option value="AGE_7_9">7-9 лет</option><option value="AGE_10_12" selected>10-12 лет</option><option value="AGE_13_15">13-15 лет</option><option value="AGE_16_17">16-17 лет</option></select></label>
            <label>Тип <select id="task-type-select" name="taskType"><option value="PRACTICE" selected>Практика</option><option value="TEST">Тест</option><option value="BUG_FIX">Исправление ошибки</option><option value="MINI_PROJECT">Мини-проект</option><option value="HOMEWORK_WITH_CRITERIA">Домашнее задание</option></select></label>
          </div>
          <label>Тема <input name="topic" value="Циклы for" required maxlength="255" /></label>
          <div id="question-count-row" class="hidden"><label>Количество вопросов <input type="number" name="questionCount" min="1" max="30" value="5" /></label></div>
          <label>Дополнительные пожелания <textarea name="additionalRequirements" placeholder="Уточнения, особые требования..."></textarea></label>
          <button id="generate-submit" class="button" type="submit">Сгенерировать задание</button>
        </form>
        <div id="generator-status"></div>
      </section>
      <section class="card">
        <h2>Результат</h2>
        <div id="generator-placeholder" class="placeholder"><p><strong>Здесь появится сгенерированное задание</strong>Заполните форму и отправьте запрос.</p></div>
        <div id="generator-result" class="details hidden"></div>
      </section>
    </div>`;

  const taskTypeSelect = document.querySelector("#task-type-select");
  const questionCountRow = document.querySelector("#question-count-row");
  taskTypeSelect.addEventListener("change", () => {
    questionCountRow.classList.toggle("hidden", taskTypeSelect.value !== "TEST");
  });

  document.querySelector("#task-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const button = document.querySelector("#generate-submit");
    const status = document.querySelector("#generator-status");
    const placeholder = document.querySelector("#generator-placeholder");
    const result = document.querySelector("#generator-result");
    button.disabled = true;
    button.textContent = "Генерация...";
    result.classList.add("hidden");
    placeholder.classList.add("hidden");
    setStatus(status, "Генерируем и сохраняем задание...", "loading");
    try {
      const taskType = form.taskType.value;
      let additionalRequirements = form.additionalRequirements.value.trim() || null;
      if (taskType === "TEST") {
        const count = parseInt(form.questionCount?.value) || 5;
        const countText = `${count} вопросов`;
        additionalRequirements = additionalRequirements ? `${countText}. ${additionalRequirements}` : countText;
      }
      const task = await request(`${tasksApiUrl}/generate`, {
        method: "POST",
        body: JSON.stringify({
          subjectArea: form.subjectArea.value,
          topic: form.topic.value.trim(),
          difficulty: form.difficulty.value,
          gradeLevel: form.gradeLevel.value,
          taskType,
          additionalRequirements,
        }),
      });
      result.innerHTML = renderTaskContent(task.generatedContent || {}, { audience: "teacher", fallbackTaskType: task.taskType });
      result.classList.remove("hidden");
      setStatus(status, "Задание успешно сгенерировано.", "success");
    } catch (error) {
      placeholder.classList.remove("hidden");
      setStatus(status, error.message, "error");
    } finally {
      button.disabled = false;
      button.textContent = "Сгенерировать задание";
    }
  });
}

async function renderTeacherTasksView() {
  el.content.innerHTML = `
    <div class="view-header"><div><h2>Мои задания</h2><p>Выберите задание и назначьте его одной или нескольким группам.</p></div><button id="refresh-tasks" class="button secondary">Обновить</button></div>
    <div class="grid-2"><section class="card"><h2>Список заданий</h2><div id="tasks-status"></div><div id="teacher-tasks-list" class="list list-scroll"></div></section><section class="card"><h2>Назначение</h2><div id="task-assignment-panel" class="placeholder"><p><strong>Выберите задание</strong>После выбора появится список групп для назначения.</p></div></section></div>`;
  document.querySelector("#refresh-tasks").addEventListener("click", loadTeacherTasks);
  await loadTeacherTasks();
}

async function loadTeacherTasks() {
  const status = document.querySelector("#tasks-status");
  const list = document.querySelector("#teacher-tasks-list");
  setStatus(status, "Загружаем задания...", "loading");
  try {
    state.teacherTasks = await request(tasksApiUrl);
    state.groups = await request(groupsApiUrl);
    list.innerHTML = state.teacherTasks.map(taskCardHtml).join("") || `<div class="placeholder"><p><strong>Заданий пока нет</strong>Сначала сгенерируйте задание.</p></div>`;
    list.querySelectorAll("[data-task-id]").forEach((button) => button.addEventListener("click", () => showTaskAssignment(button.dataset.taskId)));
    setStatus(status, "", "");
  } catch (error) {
    setStatus(status, error.message, "error");
  }
}

function showTaskAssignment(taskId) {
  state.selectedTask = state.teacherTasks.find((task) => task.id === taskId);
  const panel = document.querySelector("#task-assignment-panel");
  panel.className = "";
  panel.innerHTML = `
    <div class="task-panel-header">
      <button class="button secondary small" id="edit-task-btn" type="button">Редактировать</button>
    </div>
    <div class="details">${renderTaskContent(state.selectedTask.generatedContent || {}, { audience: "teacher", fallbackTaskType: state.selectedTask.taskType })}</div>
    <form id="assign-form" class="section">
      <h3>Назначить группам</h3>
      <div class="checkbox-list">
        ${state.groups.map((group) => `<label class="checkbox-row"><input type="checkbox" name="groupIds" value="${group.id}" /> ${escapeHtml(group.name)}</label>`).join("") || "<p>Сначала создайте группу.</p>"}
      </div>
      <label>Дедлайн <span class="field-hint">(необязательно)</span><input type="datetime-local" name="deadline" /></label>
      <button class="button" type="submit">Назначить</button>
    </form>
    <div id="assign-status"></div>`;
  document.querySelector("#edit-task-btn").addEventListener("click", () => showEditForm(state.selectedTask));
  document.querySelector("#assign-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const ids = [...event.currentTarget.querySelectorAll("input:checked")].map((input) => input.value);
    const status = document.querySelector("#assign-status");
    if (ids.length === 0) {
      setStatus(status, "Выберите хотя бы одну группу.", "warning");
      return;
    }
    try {
      const deadlineValue = event.currentTarget.deadline?.value;
      const deadline = deadlineValue ? new Date(deadlineValue).toISOString() : null;
      await request(`${tasksApiUrl}/${taskId}/assign-groups`, { method: "POST", body: JSON.stringify({ groupIds: ids, deadline }) });
      setStatus(status, "Задание назначено выбранным группам.", "success");
    } catch (error) {
      setStatus(status, error.message, "error");
    }
  });
}

function showEditForm(task) {
  const content = task.generatedContent || {};
  const taskType = (task.taskType || content.taskType || "PRACTICE").toUpperCase();
  const panel = document.querySelector("#task-assignment-panel");
  panel.className = "";
  panel.innerHTML = `
    <form id="edit-form">
      <div class="details">
        <div class="section">
          <h3>Редактирование задания</h3>
          <div class="edit-fields">
            <label>Название <input name="title" value="${escapeHtml(content.title || "")}" required /></label>
            <label>Описание <textarea name="description">${escapeHtml(content.description || "")}</textarea></label>
            <label>Инструкция <textarea name="instructions">${escapeHtml(content.instructions || "")}</textarea></label>
            <label>Ожидаемый результат <textarea name="expectedResult">${escapeHtml(content.expectedResult || "")}</textarea></label>
            <label>Подсказки <span class="field-hint">(по одной на строку)</span><textarea name="hints">${escapeHtml((content.hints || []).join("\n"))}</textarea></label>
            ${buildEditTypeFields(content, taskType)}
          </div>
        </div>
      </div>
      <div class="edit-actions">
        <button class="button" type="submit">Сохранить</button>
        <button class="button secondary" type="button" id="cancel-edit">Отмена</button>
      </div>
      <div id="edit-status"></div>
    </form>`;
  document.querySelector("#cancel-edit").addEventListener("click", () => showTaskAssignment(task.id));
  document.querySelector("#edit-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const status = document.querySelector("#edit-status");
    const splitLines = (val) => (val || "").split("\n").map((s) => s.trim()).filter(Boolean);
    const updatedContent = {
      taskType,
      title: form.title.value.trim(),
      description: form.description.value.trim(),
      instructions: form.instructions.value.trim(),
      expectedResult: form.expectedResult.value.trim(),
      hints: splitLines(form.hints.value),
      ...collectEditTypeFields(form, taskType, content),
    };
    try {
      const updated = await request(`${tasksApiUrl}/${task.id}/content`, {
        method: "PATCH",
        body: JSON.stringify({ generatedContent: updatedContent }),
      });
      const idx = state.teacherTasks.findIndex((t) => t.id === task.id);
      if (idx !== -1) state.teacherTasks[idx] = updated;
      const list = document.querySelector("#teacher-tasks-list");
      if (list) {
        list.innerHTML = state.teacherTasks.map(taskCardHtml).join("");
        list.querySelectorAll("[data-task-id]").forEach((btn) => btn.addEventListener("click", () => showTaskAssignment(btn.dataset.taskId)));
      }
      setStatus(status, "Изменения сохранены.", "success");
      setTimeout(() => showTaskAssignment(task.id), 700);
    } catch (error) {
      setStatus(status, error.message, "error");
    }
  });
}

function buildEditTypeFields(content, taskType) {
  switch (taskType) {
    case "PRACTICE":
      return `
        <label>Стартовый код <textarea name="starterCode" class="code-area">${escapeHtml(content.starterCode || "")}</textarea></label>
        <label>Требования <span class="field-hint">(по одному на строку)</span><textarea name="requirements">${escapeHtml((content.requirements || []).join("\n"))}</textarea></label>
        <label>Решение преподавателя <textarea name="teacherSolution" class="code-area">${escapeHtml(content.teacherSolution || "")}</textarea></label>`;
    case "TEST":
      return `<label>Проходной балл <input name="passingScore" value="${escapeHtml(content.passingScore || "")}" /></label>`;
    case "BUG_FIX":
      return `
        <label>Код с ошибкой <textarea name="buggyCode" class="code-area">${escapeHtml(content.buggyCode || "")}</textarea></label>
        <label>Описание ошибки <textarea name="bugDescription">${escapeHtml(content.bugDescription || "")}</textarea></label>
        <label>Ожидаемое поведение <textarea name="expectedFixedBehavior">${escapeHtml(content.expectedFixedBehavior || "")}</textarea></label>
        <label>Решение преподавателя <textarea name="teacherSolution" class="code-area">${escapeHtml(content.teacherSolution || "")}</textarea></label>
        <label>Типичные ошибки <span class="field-hint">(по одной на строку)</span><textarea name="commonMistakes">${escapeHtml((content.commonMistakes || []).join("\n"))}</textarea></label>`;
    case "MINI_PROJECT":
      return `
        <label>Цель проекта <textarea name="projectGoal">${escapeHtml(content.projectGoal || "")}</textarea></label>
        <label>Функциональные требования <span class="field-hint">(по одному на строку)</span><textarea name="functionalRequirements">${escapeHtml((content.functionalRequirements || []).join("\n"))}</textarea></label>
        <label>Шаги выполнения <span class="field-hint">(по одному на строку)</span><textarea name="steps">${escapeHtml((content.steps || []).join("\n"))}</textarea></label>
        <label>Критерии готовности <span class="field-hint">(по одному на строку)</span><textarea name="acceptanceCriteria">${escapeHtml((content.acceptanceCriteria || []).join("\n"))}</textarea></label>
        <label>Идеи для развития <span class="field-hint">(по одной на строку)</span><textarea name="extensionIdeas">${escapeHtml((content.extensionIdeas || []).join("\n"))}</textarea></label>`;
    case "HOMEWORK_WITH_CRITERIA":
      return `
        <label>Пункты задания <span class="field-hint">(по одному на строку)</span><textarea name="homeworkTasks">${escapeHtml((content.homeworkTasks || []).join("\n"))}</textarea></label>
        <label>Максимальный балл <input type="number" name="maxScore" value="${escapeHtml(String(content.maxScore ?? 10))}" min="1" max="100" /></label>
        <label>Заметки преподавателя <textarea name="teacherNotes">${escapeHtml(content.teacherNotes || "")}</textarea></label>`;
    default:
      return "";
  }
}

function collectEditTypeFields(form, taskType, current) {
  const splitLines = (name) => (form[name]?.value || "").split("\n").map((s) => s.trim()).filter(Boolean);
  switch (taskType) {
    case "PRACTICE":
      return {
        starterCode: form.starterCode?.value ?? "",
        requirements: splitLines("requirements"),
        teacherSolution: form.teacherSolution?.value ?? "",
        example: current.example ?? { input: "", output: "" },
      };
    case "TEST":
      return {
        passingScore: form.passingScore?.value ?? "",
        questions: current.questions ?? [],
      };
    case "BUG_FIX":
      return {
        buggyCode: form.buggyCode?.value ?? "",
        bugDescription: form.bugDescription?.value ?? "",
        expectedFixedBehavior: form.expectedFixedBehavior?.value ?? "",
        teacherSolution: form.teacherSolution?.value ?? "",
        commonMistakes: splitLines("commonMistakes"),
      };
    case "MINI_PROJECT":
      return {
        projectGoal: form.projectGoal?.value ?? "",
        functionalRequirements: splitLines("functionalRequirements"),
        steps: splitLines("steps"),
        acceptanceCriteria: splitLines("acceptanceCriteria"),
        extensionIdeas: splitLines("extensionIdeas"),
      };
    case "HOMEWORK_WITH_CRITERIA":
      return {
        homeworkTasks: splitLines("homeworkTasks"),
        maxScore: parseInt(form.maxScore?.value) || 10,
        teacherNotes: form.teacherNotes?.value ?? "",
        evaluationCriteria: current.evaluationCriteria ?? [],
      };
    default:
      return {};
  }
}

async function renderGroupsView() {
  el.content.innerHTML = `
    <div class="view-header"><div><h2>Группы</h2><p>Создавайте группы и добавляйте учеников по email.</p></div></div>
    <div class="grid-2">
      <section class="card">
        <h2>Создать группу</h2>
        <form id="group-form"><label>Название <input name="name" required maxlength="150" /></label><label>Описание <textarea name="description"></textarea></label><button class="button">Создать</button></form>
        <div id="group-status"></div>
        <div class="section"><h3>Ваши группы</h3><div id="groups-list" class="list"></div></div>
      </section>
      <section class="card"><h2>Ученики группы</h2><div id="group-detail" class="placeholder"><p><strong>Выберите группу</strong>Здесь появится список учеников.</p></div></section>
    </div>`;
  document.querySelector("#group-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const status = document.querySelector("#group-status");
    try {
      await request(groupsApiUrl, { method: "POST", body: JSON.stringify({ name: form.name.value.trim(), description: form.description.value.trim() || null }) });
      form.reset();
      setStatus(status, "Группа создана.", "success");
      await loadGroups();
    } catch (error) {
      setStatus(status, error.message, "error");
    }
  });
  await loadGroups();
}

async function loadGroups() {
  state.groups = await request(groupsApiUrl);
  const list = document.querySelector("#groups-list");
  list.innerHTML = state.groups.map((group) => `<button class="item-card" data-group-id="${group.id}"><span class="item-title">${escapeHtml(group.name)}</span><span class="muted">${escapeHtml(group.description || "Без описания")}</span></button>`).join("") || `<div class="placeholder"><p><strong>Групп пока нет</strong>Создайте первую группу.</p></div>`;
  list.querySelectorAll("[data-group-id]").forEach((button) => button.addEventListener("click", () => showGroupDetail(button.dataset.groupId)));
}

async function showGroupDetail(groupId) {
  state.selectedGroup = state.groups.find((group) => group.id === groupId);
  const detail = document.querySelector("#group-detail");
  detail.className = "";
  detail.innerHTML = `<p class="muted">Загружаем учеников...</p>`;
  const students = await request(`${groupsApiUrl}/${groupId}/students`);
  detail.innerHTML = `
    <h3>${escapeHtml(state.selectedGroup.name)}</h3>
    <form id="add-student-form"><label>Email ученика <input name="email" type="email" required /></label><button class="button">Добавить ученика</button></form>
    <div id="student-add-status"></div>
    <div class="section"><h3>Ученики</h3><div class="list">${students.map((student) => `<div class="item-card"><strong>${escapeHtml(student.name)}</strong><p>${escapeHtml(student.email)}</p></div>`).join("") || "<p>В группе пока нет учеников.</p>"}</div></div>`;
  document.querySelector("#add-student-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const status = document.querySelector("#student-add-status");
    try {
      await request(`${groupsApiUrl}/${groupId}/students`, { method: "POST", body: JSON.stringify({ email: event.currentTarget.email.value.trim() }) });
      setStatus(status, "Ученик добавлен.", "success");
      await showGroupDetail(groupId);
    } catch (error) {
      setStatus(status, error.message, "error");
    }
  });
}

async function renderReviewView() {
  el.content.innerHTML = `
    <div class="view-header"><div><h2>Проверка решений</h2><p>Выберите задание, затем откройте отправленное решение.</p></div></div>
    <div class="grid-2"><section class="card"><h2>Задания</h2><div id="review-tasks" class="list list-scroll"></div></section><section class="card"><h2>Решения</h2><div id="review-panel" class="placeholder"><p><strong>Выберите задание</strong>Здесь появятся решения учеников.</p></div></section></div>`;
  state.teacherTasks = await request(tasksApiUrl);
  const list = document.querySelector("#review-tasks");
  list.innerHTML = state.teacherTasks.map(taskCardHtml).join("") || "<p>Заданий пока нет.</p>";
  list.querySelectorAll("[data-task-id]").forEach((button) => button.addEventListener("click", () => loadSubmissions(button.dataset.taskId)));
}

async function loadSubmissions(taskId) {
  const panel = document.querySelector("#review-panel");
  panel.className = "";
  panel.innerHTML = `<p class="muted">Загружаем решения...</p>`;
  state.submissions = await request(`${tasksApiUrl}/${taskId}/submissions`);
  panel.innerHTML = `<div class="list">${state.submissions.map((submission) => `<button class="item-card" data-submission-id="${submission.id}"><span class="item-title">${escapeHtml(submission.studentName || submission.studentEmail)}</span><span class="badge ${submissionStatusBadge(submission.status)}">${submissionStatusLabel(submission.status)}</span><p>${escapeHtml(submission.answerText)}</p>${submission.answerUrl ? `<p><a href="${escapeHtml(submission.answerUrl)}" target="_blank" rel="noopener">Ссылка на работу</a></p>` : ""}</button>`).join("") || "<p>Решений пока нет.</p>"}</div><div id="review-form-panel" class="section"></div>`;
  panel.querySelectorAll("[data-submission-id]").forEach((button) => button.addEventListener("click", () => showReviewForm(button.dataset.submissionId)));
}

function showReviewForm(submissionId) {
  const submission = state.submissions.find((item) => item.id === submissionId);
  document.querySelector("#review-form-panel").innerHTML = `
    <h3>Проверка решения</h3>
    <p>${escapeHtml(submission.answerText)}</p>
    ${submission.answerUrl ? `<p><a href="${escapeHtml(submission.answerUrl)}" target="_blank" rel="noopener">Ссылка на работу</a></p>` : ""}
    <form id="review-form">
      <label>Оценка <input name="grade" type="number" min="1" max="5" value="${submission.grade || ""}" /></label>
      <label>Комментарий <textarea name="teacherComment">${escapeHtml(submission.teacherComment || "")}</textarea></label>
      <div class="review-actions">
        <button class="button" type="submit" name="reviewStatus" value="REVIEWED">Зачтено</button>
        <button class="button secondary" type="submit" name="reviewStatus" value="NEEDS_REVISION">Нужно доработать</button>
      </div>
    </form>
    <div id="review-status"></div>`;
  document.querySelector("#review-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const reviewStatus = event.submitter?.value || "REVIEWED";
    const status = document.querySelector("#review-status");
    try {
      await request(`${submissionsApiUrl}/${submissionId}/review`, {
        method: "PATCH",
        body: JSON.stringify({ status: reviewStatus, grade: form.grade.value ? Number(form.grade.value) : null, teacherComment: form.teacherComment.value.trim() || null }),
      });
      setStatus(status, reviewStatus === "REVIEWED" ? "Решение зачтено." : "Задание отправлено на доработку.", "success");
    } catch (error) {
      setStatus(status, error.message, "error");
    }
  });
}

async function renderStudentGroupsView() {
  el.content.innerHTML = `
    <div class="view-header"><div><h2>Мои группы</h2><p>Вступайте в группы по коду приглашения от преподавателя.</p></div></div>
    <div class="grid-2">
      <section class="card">
        <h2>Вступить в группу</h2>
        <form id="join-group-form">
          <label>Код приглашения <input name="inviteCode" required maxlength="16" placeholder="Например: AB3K7PQR" style="text-transform:uppercase" /></label>
          <button class="button" type="submit">Вступить</button>
        </form>
        <div id="join-status"></div>
      </section>
      <section class="card">
        <h2>Мои группы</h2>
        <div id="student-groups-status"></div>
        <div id="student-groups-list" class="list"></div>
      </section>
    </div>`;

  document.querySelector("#join-group-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const status = document.querySelector("#join-status");
    const input = event.currentTarget.inviteCode;
    try {
      const group = await request(`${studentGroupsApiUrl}/join`, {
        method: "POST",
        body: JSON.stringify({ inviteCode: input.value.trim().toUpperCase() }),
      });
      setStatus(status, `Вы вступили в группу «${group.name}».`, "success");
      input.value = "";
      await loadStudentGroups();
    } catch (error) {
      setStatus(status, error.message, "error");
    }
  });

  await loadStudentGroups();
}

async function loadStudentGroups() {
  const status = document.querySelector("#student-groups-status");
  const list = document.querySelector("#student-groups-list");
  if (!status || !list) return;
  setStatus(status, "Загружаем группы...", "loading");
  try {
    const groups = await request(studentGroupsApiUrl);
    list.innerHTML = groups.map((group) => `
      <div class="item-card">
        <strong>${escapeHtml(group.name)}</strong>
        ${group.description ? `<p>${escapeHtml(group.description)}</p>` : ""}
      </div>`).join("") || `<div class="placeholder"><p><strong>Вы пока не в группах</strong>Введите код приглашения, чтобы вступить.</p></div>`;
    setStatus(status, "", "");
  } catch (error) {
    setStatus(status, error.message, "error");
  }
}

async function renderStudentTasksView() {
  el.content.innerHTML = `
    <div class="view-header"><div><h2>Мои задания</h2><p>Задания, назначенные вашим группам.</p></div><button id="refresh-student-tasks" class="button secondary">Обновить</button></div>
    <div class="grid-2"><section class="card"><h2>Список</h2><div id="student-tasks-status"></div><div id="student-tasks-list" class="list list-scroll"></div></section><section class="card"><h2>Задание и решение</h2><div id="student-task-detail" class="placeholder"><p><strong>Выберите задание</strong>Здесь появится форма ответа.</p></div></section></div>`;
  document.querySelector("#refresh-student-tasks").addEventListener("click", loadStudentTasks);
  await loadStudentTasks();
}

async function loadStudentTasks() {
  const status = document.querySelector("#student-tasks-status");
  const list = document.querySelector("#student-tasks-list");
  setStatus(status, "Загружаем задания...", "loading");
  try {
    const tasks = await request(studentTasksApiUrl);
    list.innerHTML = tasks.map(taskCardHtml).join("") || `<div class="placeholder"><p><strong>Назначенных заданий пока нет</strong>Преподаватель ещё не назначил задания вашей группе.</p></div>`;
    list.querySelectorAll("[data-task-id]").forEach((button) => button.addEventListener("click", () => showStudentTask(button.dataset.taskId)));
    setStatus(status, "", "");
  } catch (error) {
    setStatus(status, error.message, "error");
  }
}

async function showStudentTask(taskId) {
  const detail = document.querySelector("#student-task-detail");
  detail.className = "";
  detail.innerHTML = `<p class="muted">Загружаем задание...</p>`;
  const task = await request(`${studentTasksApiUrl}/${taskId}`);
  const submission = task.submission;
  const deadlineHtml = task.deadline
    ? `<div class="section"><h3>Дедлайн</h3><p>${formatDate(task.deadline)}</p></div>`
    : "";
  detail.innerHTML = `
    <div class="details">${renderTaskContent(task.generatedContent || {}, { audience: "student", fallbackTaskType: task.taskType })}</div>
    ${deadlineHtml}
    <form id="submit-form" class="section">
      <h3>Моё решение</h3>
      <textarea name="answerText" required>${escapeHtml(submission?.answerText || "")}</textarea>
      <label>Ссылка на работу <span class="field-hint">(необязательно)</span><input name="answerUrl" type="url" value="${escapeHtml(submission?.answerUrl || "")}" placeholder="https://..." /></label>
      <button class="button">Отправить решение</button>
    </form>
    <div class="section">
      <h3>Статус</h3>
      <p><span class="badge ${submissionStatusBadge(submission?.status)}">${submissionStatusLabel(submission?.status)}</span></p>
      ${submission?.teacherComment ? `<p><em>Комментарий: ${escapeHtml(submission.teacherComment)}</em></p>` : ""}
      ${submission?.grade ? `<p>Оценка: <strong>${submission.grade}</strong></p>` : ""}
    </div>
    <div id="submit-status"></div>`;
  document.querySelector("#submit-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const status = document.querySelector("#submit-status");
    try {
      const answerUrl = event.currentTarget.answerUrl.value.trim() || null;
      await request(`${studentTasksApiUrl}/${taskId}/submit`, { method: "POST", body: JSON.stringify({ answerText: event.currentTarget.answerText.value.trim(), answerUrl }) });
      setStatus(status, "Решение отправлено.", "success");
      await showStudentTask(taskId);
    } catch (error) {
      setStatus(status, error.message, "error");
    }
  });
}

function taskCardHtml(task) {
  const content = task.generatedContent || {};
  return `<button class="item-card" data-task-id="${task.id}">
    <span class="item-title">${escapeHtml(content.title || task.topic || "Без названия")}</span>
    <span class="meta"><span class="badge neutral">${escapeHtml(task.subjectArea)}</span><span class="badge neutral">${escapeHtml(task.difficulty)}</span><span class="badge neutral">${escapeHtml(task.taskType)}</span><span class="badge ${task.status === "GENERATED" ? "success" : "failed"}">${escapeHtml(task.status)}</span></span>
    <span class="muted">Создано: ${formatDate(task.createdAt)}</span>
  </button>`;
}

function renderTaskContent(content, options = {}) {
  const audience = options.audience || "teacher";
  const rawType = options.fallbackTaskType ?? content.taskType ?? "PRACTICE";
  const taskType = String(rawType).toUpperCase();
  const hints = Array.isArray(content.hints) && content.hints.length ? content.hints : ["Подсказки не заданы."];
  return `
    <section class="section"><h3>Название</h3><p>${escapeHtml(content.title || "Без названия")}</p></section>
    <section class="section"><h3>Тип задания</h3><p>${escapeHtml(taskType)}</p></section>
    <section class="section"><h3>Описание</h3><p>${escapeHtml(content.description || "Описание не задано.")}</p></section>
    <section class="section"><h3>Инструкция</h3><p>${escapeHtml(content.instructions || "Инструкция не задана.")}</p></section>
    <section class="section"><h3>Входные данные</h3><p>${escapeHtml(content.inputData || "Не требуется.")}</p></section>
    <section class="section"><h3>Ожидаемый результат</h3><p>${escapeHtml(content.expectedResult || "Ожидаемый результат не задан.")}</p></section>
    <section class="section"><h3>Подсказки</h3><ul>${hints.map((hint) => `<li>${escapeHtml(hint)}</li>`).join("")}</ul></section>
    ${renderTypeSpecificContent(content, taskType, audience)}`;
}

function renderTypeSpecificContent(content, taskType, audience) {
  if (taskType === "TEST") return renderTestContent(content, audience);
  if (taskType === "BUG_FIX") return renderBugFixContent(content, audience);
  if (taskType === "MINI_PROJECT") return renderMiniProjectContent(content);
  if (taskType === "HOMEWORK_WITH_CRITERIA") return renderHomeworkContent(content, audience);
  return renderPracticeContent(content, audience);
}

function renderPracticeContent(content, audience) {
  return `
    ${renderCodeSection("Стартовый код", content.starterCode)}
    ${renderListSection("Требования", content.requirements)}
    ${content.example ? `<section class="section"><h3>Пример</h3><p>Ввод: ${escapeHtml(content.example.input || "")}</p><p>Вывод: ${escapeHtml(content.example.output || "")}</p></section>` : ""}
    ${audience === "teacher" ? renderCodeSection("Решение преподавателя", content.teacherSolution) : ""}`;
}

function renderTestContent(content, audience) {
  const questions = Array.isArray(content.questions) ? content.questions : [];
  const optionLabels = ["А", "Б", "В", "Г"];
  const body =
    questions.length > 0
      ? questions
          .map((question, index) => {
            const options = question.options || [];
            const correctIdx = options.indexOf(question.correctAnswer);
            const optionsHtml = options
              .map((option, i) => {
                const isCorrect = audience === "teacher" && i === correctIdx;
                return `<li class="test-option${isCorrect ? " correct" : ""}">
                  <span class="option-label">${optionLabels[i] || i + 1})</span> ${escapeHtml(option)}
                </li>`;
              })
              .join("");
            const explanationHtml =
              audience === "teacher" && question.explanation
                ? `<p class="test-explanation"><em>Пояснение: ${escapeHtml(question.explanation)}</em></p>`
                : "";
            return `
              <div class="item-card">
                <strong>${index + 1}. ${escapeHtml(question.question || "")}</strong>
                <ul class="test-options">${optionsHtml}</ul>
                ${explanationHtml}
              </div>`;
          })
          .join("")
      : `<p class="muted">В сохранённом задании нет вопросов. Сгенерируйте задание заново.</p>`;
  return `
    <section class="section"><h3>Вопросы</h3>
      ${body}
    </section>
    <section class="section"><h3>Проходной балл</h3><p>${escapeHtml(content.passingScore || "Не указан")}</p></section>`;
}

function renderBugFixContent(content, audience) {
  return `
    ${renderCodeSection("Код с ошибкой", content.buggyCode)}
    <section class="section"><h3>Описание ошибки</h3><p>${escapeHtml(content.bugDescription || "Не указано")}</p></section>
    <section class="section"><h3>Ожидаемое поведение</h3><p>${escapeHtml(content.expectedFixedBehavior || "Не указано")}</p></section>
    ${renderListSection("Типичные ошибки", content.commonMistakes)}
    ${audience === "teacher" ? renderCodeSection("Решение преподавателя", content.teacherSolution) : ""}`;
}

function renderMiniProjectContent(content) {
  return `
    <section class="section"><h3>Цель проекта</h3><p>${escapeHtml(content.projectGoal || "Не указано")}</p></section>
    ${renderListSection("Функциональные требования", content.functionalRequirements)}
    ${renderListSection("Шаги выполнения", content.steps)}
    ${renderListSection("Критерии готовности", content.acceptanceCriteria)}
    ${renderListSection("Идеи для развития", content.extensionIdeas)}`;
}

function renderHomeworkContent(content, audience) {
  return `
    ${renderListSection("Пункты домашнего задания", content.homeworkTasks)}
    <section class="section"><h3>Критерии оценивания</h3>
      <ul>${(content.evaluationCriteria || []).map((item) => `<li>${escapeHtml(item.criterion || "")}: ${escapeHtml(item.points ?? "")} балл(ов)</li>`).join("")}</ul>
      <p>Максимальный балл: ${escapeHtml(content.maxScore ?? "10")}</p>
    </section>
    ${audience === "teacher" ? `<section class="section"><h3>Заметки преподавателя</h3><p>${escapeHtml(content.teacherNotes || "Не указано")}</p></section>` : ""}`;
}

function renderListSection(title, items) {
  const list = Array.isArray(items) && items.length ? items : ["Не указано"];
  return `<section class="section"><h3>${title}</h3><ul>${list.map((item) => `<li>${escapeHtml(item)}</li>`).join("")}</ul></section>`;
}

function renderCodeSection(title, value) {
  if (!value) return "";
  return `<section class="section"><h3>${title}</h3><pre><code>${escapeHtml(value)}</code></pre></section>`;
}

function clearSession(message) {
  state.accessToken = null;
  state.currentUser = null;
  localStorage.removeItem(tokenKey);
  renderAuthView("login");
  setStatus(el.authStatus, message || "", message ? "error" : "");
}

function setStatus(target, message, type) {
  target.textContent = message;
  target.className = message ? `status ${type}` : "";
}

function formatDate(value) {
  if (!value) return "Не указано";
  return new Intl.DateTimeFormat("ru-RU", { dateStyle: "short", timeStyle: "short" }).format(new Date(value));
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function submissionStatusLabel(status) {
  if (!status) return "Не начато";
  if (status === "SUBMITTED") return "Отправлено";
  if (status === "REVIEWED") return "Зачтено";
  if (status === "NEEDS_REVISION") return "Нужно доработать";
  return status;
}

function submissionStatusBadge(status) {
  if (status === "REVIEWED") return "success";
  if (status === "NEEDS_REVISION") return "failed";
  return "neutral";
}
