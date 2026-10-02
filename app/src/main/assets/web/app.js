/**
 * SR UNIVERSITY EXAMVAULT - ACADEMIC WEB APPLICATION
 * Clean institutional portal: Authentication, Dashboard, Image Question Paper Archive,
 * Image Uploads, and Contributor Leaderboard.
 * Blue & White theme. No emojis.
 */

// =============================================================================
// STATE & STORAGE INITIALIZATION
// =============================================================================

const STORAGE_KEYS = {
  USER: 'sru_vault_user_v2',
  PAPERS: 'sru_vault_papers_v2',
  SAVED: 'sru_vault_saved_v2',
  LEADERBOARD: 'sru_vault_leaders_v2'
};

// Current Session State
let currentUser = null;
let papersList = [];
let savedPaperIds = new Set();
let currentFilter = {
  branch: 'ALL',
  exam: 'ALL',
  semester: 0,
  search: ''
};
let selectedUploadImage = null;
let currentZoom = 1.0;

// High-resolution SVG-based Official SR University Question Paper Image Generator
function createOfficialPaperImage(code, title, branch, sem, examType, monthYear, regulation) {
  const isMid = examType.includes('Mid');
  const maxMarks = isMid ? '30' : '70';
  const duration = isMid ? '90 Minutes' : '3 Hours';

  const svgContent = `
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 1100" width="800" height="1100" style="background:#ffffff; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
    <!-- Paper Sheet Outline -->
    <rect x="0" y="0" width="800" height="1100" fill="#ffffff"/>
    <rect x="25" y="25" width="750" height="1050" fill="none" stroke="#1E3A8A" stroke-width="2"/>
    <rect x="30" y="30" width="740" height="1040" fill="none" stroke="#93C5FD" stroke-width="1"/>

    <!-- University Header -->
    <text x="400" y="80" text-anchor="middle" font-size="24" font-weight="900" fill="#1E3A8A" letter-spacing="1">SR UNIVERSITY</text>
    <text x="400" y="105" text-anchor="middle" font-size="13" font-weight="700" fill="#1D4ED8" letter-spacing="0.5">ANANTHASAGAR, WARANGAL, TELANGANA - 506371</text>
    <text x="400" y="128" text-anchor="middle" font-size="12" font-weight="600" fill="#475569">EXAMINATION BRANCH &bull; B.TECH DEGREE EXAMINATIONS</text>

    <!-- Exam Details Banner -->
    <rect x="50" y="145" width="700" height="32" fill="#EFF6FF" stroke="#BFDBFE" stroke-width="1" rx="4"/>
    <text x="400" y="166" text-anchor="middle" font-size="14" font-weight="800" fill="#1E40AF">
      ${examType.toUpperCase()} EXAMINATION &bull; ${monthYear.toUpperCase()}
    </text>

    <!-- Branch, Semester & Regulation -->
    <text x="60" y="205" font-size="13" font-weight="700" fill="#0F172A">BRANCH: ${branch} (SEMESTER ${sem})</text>
    <text x="620" y="205" font-size="13" font-weight="700" fill="#0F172A">REGULATION: ${regulation}</text>

    <!-- Subject Code & Title -->
    <rect x="50" y="220" width="700" height="42" fill="#F8FAFC" stroke="#E2E8F0" stroke-width="1" rx="4"/>
    <text x="70" y="246" font-size="15" font-weight="900" fill="#1E3A8A">COURSE CODE: ${code}</text>
    <text x="290" y="246" font-size="15" font-weight="900" fill="#0F172A">${title.toUpperCase()}</text>

    <!-- Time and Marks -->
    <line x1="50" y1="275" x2="750" y2="275" stroke="#CBD5E1" stroke-width="1"/>
    <text x="60" y="295" font-size="12.5" font-weight="700" fill="#334155">Time: ${duration}</text>
    <text x="620" y="295" font-size="12.5" font-weight="700" fill="#334155">Max. Marks: ${maxMarks}</text>
    <line x1="50" y1="305" x2="750" y2="305" stroke="#CBD5E1" stroke-width="1"/>

    <!-- General Instructions -->
    <text x="60" y="330" font-size="11.5" font-weight="700" fill="#64748B">INSTRUCTIONS TO CANDIDATES:</text>
    <text x="60" y="348" font-size="11" fill="#475569">1. Answer all questions from PART-A. In PART-B, answer any ONE full question from each unit.</text>
    <text x="60" y="364" font-size="11" fill="#475569">2. All parts of the question must be answered at one place only.</text>

    <!-- PART - A Header -->
    <rect x="50" y="385" width="700" height="26" fill="#1E3A8A" rx="3"/>
    <text x="400" y="403" text-anchor="middle" font-size="12" font-weight="800" fill="#ffffff">PART - A (Compulsory Questions &bull; 10 Marks)</text>

    <!-- Part A Questions -->
    <text x="60" y="435" font-size="12" font-weight="700" fill="#1E3A8A">Q1. a)</text>
    <text x="110" y="435" font-size="12" fill="#0F172A">State the primary design objectives and define asymptotic notations with graphical limits.</text>
    <text x="710" y="435" font-size="11.5" font-weight="700" fill="#64748B">[2M]</text>

    <text x="60" y="470" font-size="12" font-weight="700" fill="#1E3A8A">    b)</text>
    <text x="110" y="470" font-size="12" fill="#0F172A">Illustrate the fundamental theorem used in solving recurrence equations with an example.</text>
    <text x="710" y="470" font-size="11.5" font-weight="700" fill="#64748B">[2M]</text>

    <text x="60" y="505" font-size="12" font-weight="700" fill="#1E3A8A">    c)</text>
    <text x="110" y="505" font-size="12" fill="#0F172A">Distinguish between greedy choice property and optimal substructure property.</text>
    <text x="710" y="505" font-size="11.5" font-weight="700" fill="#64748B">[2M]</text>

    <text x="60" y="540" font-size="12" font-weight="700" fill="#1E3A8A">    d)</text>
    <text x="110" y="540" font-size="12" fill="#0F172A">Formulate the state space tree representation for 4-Queens backtracking problem.</text>
    <text x="710" y="540" font-size="11.5" font-weight="700" fill="#64748B">[2M]</text>

    <text x="60" y="575" font-size="12" font-weight="700" fill="#1E3A8A">    e)</text>
    <text x="110" y="575" font-size="12" fill="#0F172A">Define NP-Hard and NP-Complete classes and establish their relationship.</text>
    <text x="710" y="575" font-size="11.5" font-weight="700" fill="#64748B">[2M]</text>

    <!-- PART - B Header -->
    <rect x="50" y="610" width="700" height="26" fill="#1E3A8A" rx="3"/>
    <text x="400" y="628" text-anchor="middle" font-size="12" font-weight="800" fill="#ffffff">PART - B (Answer 5 Questions choosing one from each Unit &bull; 5 x 12 = 60 Marks)</text>

    <!-- Unit 1 -->
    <text x="400" y="660" text-anchor="middle" font-size="11.5" font-weight="800" fill="#1D4ED8">UNIT - I</text>
    <text x="60" y="685" font-size="12" font-weight="700" fill="#1E3A8A">Q2. a)</text>
    <text x="110" y="685" font-size="12" fill="#0F172A">Explain Merge Sort algorithm with complete recurrence relation and solve for worst-case time.</text>
    <text x="710" y="685" font-size="11.5" font-weight="700" fill="#64748B">[7M]</text>

    <text x="60" y="715" font-size="12" font-weight="700" fill="#1E3A8A">    b)</text>
    <text x="110" y="715" font-size="12" fill="#0F172A">Trace the algorithm on the input array: [38, 27, 43, 3, 9, 82, 10].</text>
    <text x="710" y="715" font-size="11.5" font-weight="700" fill="#64748B">[5M]</text>

    <text x="400" y="745" text-anchor="middle" font-size="11" font-weight="700" fill="#64748B">(OR)</text>

    <text x="60" y="775" font-size="12" font-weight="700" fill="#1E3A8A">Q3. a)</text>
    <text x="110" y="775" font-size="12" fill="#0F172A">Analyze Quick Sort when pivot is selected as median-of-three versus first element.</text>
    <text x="710" y="775" font-size="11.5" font-weight="700" fill="#64748B">[7M]</text>

    <text x="60" y="805" font-size="12" font-weight="700" fill="#1E3A8A">    b)</text>
    <text x="110" y="805" font-size="12" fill="#0F172A">Discuss the best case and worst case scenarios with detailed recursion trees.</text>
    <text x="710" y="805" font-size="11.5" font-weight="700" fill="#64748B">[5M]</text>

    <!-- Unit 2 -->
    <text x="400" y="845" text-anchor="middle" font-size="11.5" font-weight="800" fill="#1D4ED8">UNIT - II</text>
    <text x="60" y="870" font-size="12" font-weight="700" fill="#1E3A8A">Q4.</text>
    <text x="110" y="870" font-size="12" fill="#0F172A">Construct the optimal binary search tree for keys (k1, k2, k3) with given access probabilities.</text>
    <text x="710" y="870" font-size="11.5" font-weight="700" fill="#64748B">[12M]</text>

    <text x="400" y="900" text-anchor="middle" font-size="11" font-weight="700" fill="#64748B">(OR)</text>

    <text x="60" y="930" font-size="12" font-weight="700" fill="#1E3A8A">Q5.</text>
    <text x="110" y="930" font-size="12" fill="#0F172A">Apply Dijkstra's single source shortest path algorithm on the directed graph with 6 vertices.</text>
    <text x="710" y="930" font-size="11.5" font-weight="700" fill="#64748B">[12M]</text>

    <!-- Official Stamp & Seal -->
    <rect x="580" y="980" width="160" height="50" fill="none" stroke="#1D4ED8" stroke-width="1.5" stroke-dasharray="4,2" rx="4"/>
    <text x="660" y="1000" text-anchor="middle" font-size="11" font-weight="800" fill="#1D4ED8">SR UNIVERSITY</text>
    <text x="660" y="1018" text-anchor="middle" font-size="9" font-weight="700" fill="#1E40AF">CONTROLLER OF EXAMINATIONS</text>

    <text x="60" y="1025" font-size="10.5" fill="#64748B">Verified Academic Paper Scan &bull; SR University Question Paper Vault</text>
  </svg>
  `;
  return `data:image/svg+xml;utf8,${encodeURIComponent(svgContent)}`;
}

// Clean Initial Dataset - All question papers erased per user request
function getInitialPapers() {
  return [];
}

function getInitialLeaderboard() {
  return [
    { rank: 1, name: 'Aitha Venkata Sai', branch: 'CSE', uploads: 8, points: 400 },
    { rank: 2, name: 'Sneha Rao', branch: 'AIML', uploads: 6, points: 300 },
    { rank: 3, name: 'Vikram Reddy', branch: 'ECE', uploads: 5, points: 250 },
    { rank: 4, name: 'Rohan Sharma', branch: 'CSE', uploads: 4, points: 200 },
    { rank: 5, name: 'Priya Patel', branch: 'EEE', uploads: 3, points: 150 },
    { rank: 6, name: 'Arjun Das', branch: 'MECH', uploads: 2, points: 100 }
  ];
}

// =============================================================================
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
  initStorage();
  checkAuth();
});

function initStorage() {
  const savedUser = localStorage.getItem(STORAGE_KEYS.USER);
  if (savedUser) {
    try {
      currentUser = JSON.parse(savedUser);
    } catch (e) {
      currentUser = null;
    }
  }

  // Erase all papers as requested by user
  localStorage.removeItem(STORAGE_KEYS.PAPERS);
  localStorage.removeItem(STORAGE_KEYS.SAVED);
  localStorage.removeItem('sru_vault_papers_v2');
  localStorage.removeItem('sru_vault_saved_v2');
  localStorage.removeItem('sru_exam_vault_papers');
  localStorage.removeItem('sru_saved_papers');

  papersList = [];
  savedPaperIds = new Set();
  localStorage.setItem(STORAGE_KEYS.PAPERS, JSON.stringify([]));
  localStorage.setItem(STORAGE_KEYS.SAVED, JSON.stringify([]));
}

function eraseAllPapers() {
  papersList = [];
  savedPaperIds = new Set();
  localStorage.setItem(STORAGE_KEYS.PAPERS, JSON.stringify([]));
  localStorage.setItem(STORAGE_KEYS.SAVED, JSON.stringify([]));
  if (currentUser) {
    currentUser.uploads = 0;
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(currentUser));
    updateUserInterface();
  }
  showToast('All question papers have been erased');
  renderAllViews();
}

// =============================================================================
// AUTHENTICATION FLOW
// =============================================================================

function checkAuth() {
  const authScreen = document.getElementById('auth-screen');
  const appScreen = document.getElementById('app-screen');

  if (currentUser) {
    authScreen.classList.add('hidden');
    appScreen.classList.remove('hidden');
    updateUserInterface();
    renderAllViews();
  } else {
    authScreen.classList.remove('hidden');
    appScreen.classList.add('hidden');
  }
}

function switchAuthTab(tab) {
  const loginTab = document.getElementById('tab-login-btn');
  const regTab = document.getElementById('tab-register-btn');
  const loginForm = document.getElementById('login-form');
  const regForm = document.getElementById('register-form');

  if (tab === 'login') {
    loginTab.classList.add('active');
    regTab.classList.remove('active');
    loginForm.classList.remove('hidden');
    regForm.classList.add('hidden');
  } else {
    loginTab.classList.remove('active');
    regTab.classList.add('active');
    loginForm.classList.add('hidden');
    regForm.classList.remove('hidden');
  }
}

function handleLogin(e) {
  e.preventDefault();
  const roll = document.getElementById('login-roll').value.trim();
  const email = document.getElementById('login-email').value.trim();

  currentUser = {
    name: 'Aitha Venkata Sai',
    roll: roll || '2103A51001',
    email: email || 'saikiran.a@sru.edu.in',
    branch: 'CSE',
    semester: 5,
    points: 150,
    uploads: 3
  };

  localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(currentUser));
  showToast('Welcome back, ' + currentUser.name);
  checkAuth();
}

function quickDemoLogin() {
  currentUser = {
    name: 'Aitha Venkata Sai',
    roll: '2103A51001',
    email: 'saikiran.a@sru.edu.in',
    branch: 'CSE',
    semester: 5,
    points: 150,
    uploads: 3
  };

  localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(currentUser));
  showToast('Signed in successfully');
  checkAuth();
}

function handleRegister(e) {
  e.preventDefault();
  const name = document.getElementById('reg-name').value.trim();
  const roll = document.getElementById('reg-roll').value.trim();
  const email = document.getElementById('reg-email').value.trim();
  const branch = document.getElementById('reg-branch').value;
  const sem = parseInt(document.getElementById('reg-sem').value, 10);

  currentUser = {
    name: name,
    roll: roll,
    email: email,
    branch: branch,
    semester: sem,
    points: 50,
    uploads: 0
  };

  localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(currentUser));
  showToast('Account created successfully');
  checkAuth();
}

function handleLogout() {
  currentUser = null;
  localStorage.removeItem(STORAGE_KEYS.USER);
  showToast('You have signed out');
  checkAuth();
}

function updateUserInterface() {
  if (!currentUser) return;

  const initials = currentUser.name.split(' ').map(n => n[0]).join('').take(2) || 'SV';
  document.getElementById('nav-user-avatar').textContent = initials;
  document.getElementById('nav-user-name').textContent = currentUser.name;
  document.getElementById('nav-user-roll').textContent = currentUser.roll;

  document.getElementById('profile-avatar-lg').textContent = initials;
  document.getElementById('profile-name-display').textContent = currentUser.name;
  document.getElementById('profile-roll-display').textContent = 'Roll Number: ' + currentUser.roll;
  document.getElementById('profile-email-display').textContent = 'Email: ' + currentUser.email;
  document.getElementById('profile-branch-display').textContent = currentUser.branch;
  document.getElementById('profile-sem-display').textContent = 'Semester ' + currentUser.semester;
  document.getElementById('profile-uploads-display').textContent = currentUser.uploads;
  document.getElementById('profile-points-display').textContent = currentUser.points;

  document.getElementById('stat-karma-points').textContent = currentUser.points;
}

// String helper
String.prototype.take = function(n) {
  return this.substring(0, n);
};

// =============================================================================
// NAVIGATION & VIEW SWITCHING
// =============================================================================

function navigateTo(viewName) {
  const views = document.querySelectorAll('.content-view');
  views.forEach(v => v.classList.remove('active'));

  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => {
    if (item.getAttribute('data-view') === viewName) {
      item.classList.add('active');
    } else {
      item.classList.remove('active');
    }
  });

  const targetView = document.getElementById('view-' + viewName);
  if (targetView) {
    targetView.classList.add('active');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  if (viewName === 'papers') {
    applyFilters();
  } else if (viewName === 'saved') {
    renderSavedPapers();
  } else if (viewName === 'leaderboard') {
    renderLeaderboard();
  } else if (viewName === 'dashboard') {
    renderDashboard();
  }
}

function selectDepartmentAndBrowse(branch) {
  setBranchFilter(branch);
  navigateTo('papers');
}

// =============================================================================
// PAPERS ARCHIVE & FILTERING
// =============================================================================

function setBranchFilter(branch) {
  currentFilter.branch = branch;
  const pills = document.querySelectorAll('#branch-pills .filter-pill');
  pills.forEach(p => {
    if (p.getAttribute('data-branch') === branch) {
      p.classList.add('active');
    } else {
      p.classList.remove('active');
    }
  });
  applyFilters();
}

function setExamFilter(exam) {
  currentFilter.exam = exam;
  const pills = document.querySelectorAll('#exam-pills .filter-pill');
  pills.forEach(p => {
    if (p.getAttribute('data-exam') === exam) {
      p.classList.add('active');
    } else {
      p.classList.remove('active');
    }
  });
  applyFilters();
}

function clearSearch() {
  document.getElementById('paper-search-input').value = '';
  applyFilters();
}

function applyFilters() {
  const searchInput = document.getElementById('paper-search-input');
  currentFilter.search = searchInput ? searchInput.value.trim().toLowerCase() : '';
  
  const semSelect = document.getElementById('sem-select');
  currentFilter.semester = semSelect ? parseInt(semSelect.value, 10) : 0;

  const filtered = papersList.filter(paper => {
    const matchesBranch = currentFilter.branch === 'ALL' || paper.branch === currentFilter.branch;
    const matchesExam = currentFilter.exam === 'ALL' || paper.examType === currentFilter.exam;
    const matchesSem = currentFilter.semester === 0 || paper.semester === currentFilter.semester;
    const matchesSearch = !currentFilter.search || 
      paper.subjectName.toLowerCase().includes(currentFilter.search) ||
      paper.subjectCode.toLowerCase().includes(currentFilter.search);

    return matchesBranch && matchesExam && matchesSem && matchesSearch;
  });

  const grid = document.getElementById('all-papers-grid');
  const countLabel = document.getElementById('papers-count-label');
  const noPapers = document.getElementById('no-papers-found');

  if (!grid) return;

  countLabel.textContent = `Showing ${filtered.length} of ${papersList.length} question papers`;

  if (filtered.length === 0) {
    grid.innerHTML = '';
    noPapers.classList.remove('hidden');
  } else {
    noPapers.classList.add('hidden');
    grid.innerHTML = filtered.map(paper => renderPaperCardHtml(paper)).join('');
  }
}

function renderPaperCardHtml(paper) {
  const isSaved = savedPaperIds.has(paper.id);
  const saveBtnText = isSaved ? 'Saved' : 'Save';
  const saveBtnClass = isSaved ? 'btn-secondary' : 'btn-outline';

  return `
    <div class="paper-card">
      <div class="paper-thumb-box" onclick="openImageViewerModal('${paper.id}')">
        <img src="${paper.imageUrl}" alt="${paper.subjectName} Paper Scan" loading="lazy">
        <span class="paper-thumb-badge">${paper.branch} &bull; ${paper.examType}</span>
        <span class="paper-sem-badge">Sem ${paper.semester}</span>
      </div>
      <div class="paper-card-body">
        <div class="paper-subject-code">${paper.subjectCode} &bull; ${paper.regulation || 'R22'}</div>
        <h4 class="paper-subject-title">${paper.subjectName}</h4>
        <div class="paper-meta-row">
          <span>${paper.monthYear}</span>
          <span>By ${paper.uploaderName}</span>
        </div>
        <div class="paper-card-footer">
          <button class="btn btn-primary btn-sm" onclick="openImageViewerModal('${paper.id}')">
            View Paper Image
          </button>
          <button class="btn ${saveBtnClass} btn-sm" onclick="toggleSavePaper('${paper.id}')">
            ${saveBtnText}
          </button>
        </div>
      </div>
    </div>
  `;
}

// =============================================================================
// SAVED PAPERS & BOOKMARKS
// =============================================================================

function toggleSavePaper(paperId) {
  if (savedPaperIds.has(paperId)) {
    savedPaperIds.delete(paperId);
    showToast('Removed from saved papers');
  } else {
    savedPaperIds.add(paperId);
    showToast('Saved to your examination repository');
  }

  localStorage.setItem(STORAGE_KEYS.SAVED, JSON.stringify(Array.from(savedPaperIds)));
  applyFilters();
  renderSavedPapers();
  updateDashboardCounts();
}

function renderSavedPapers() {
  const grid = document.getElementById('saved-papers-grid');
  const empty = document.getElementById('no-saved-papers');
  if (!grid) return;

  const savedList = papersList.filter(p => savedPaperIds.has(p.id));

  if (savedList.length === 0) {
    grid.innerHTML = '';
    empty.classList.remove('hidden');
  } else {
    empty.classList.add('hidden');
    grid.innerHTML = savedList.map(paper => renderPaperCardHtml(paper)).join('');
  }
}

// =============================================================================
// DASHBOARD RENDERING & COUNTS
// =============================================================================

function renderDashboard() {
  updateDashboardCounts();

  // Populate recent papers
  const recentGrid = document.getElementById('recent-papers-grid');
  if (recentGrid) {
    if (papersList.length === 0) {
      recentGrid.innerHTML = `
        <div class="empty-state" style="grid-column: 1 / -1; width: 100%; padding: 36px 20px;">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
          <h3>Repository is Empty</h3>
          <p>All previous question papers have been erased. Upload your first question paper image to begin archiving.</p>
          <button class="btn btn-primary btn-sm mt-2" onclick="navigateTo('upload')">Upload Question Paper Image</button>
        </div>
      `;
    } else {
      const recent = papersList.slice(0, 3);
      recentGrid.innerHTML = recent.map(p => renderPaperCardHtml(p)).join('');
    }
  }
}

function updateDashboardCounts() {
  document.getElementById('stat-total-papers').textContent = papersList.length;
  document.getElementById('stat-image-papers').textContent = papersList.length;
  document.getElementById('stat-saved-papers').textContent = savedPaperIds.size;

  // Department counts
  const depts = ['CSE', 'AIML', 'ECE', 'EEE', 'MECH', 'CIVIL'];
  depts.forEach(d => {
    const el = document.getElementById('count-' + d.toLowerCase());
    if (el) {
      const count = papersList.filter(p => p.branch === d).length;
      el.textContent = `${count} Papers`;
    }
  });
}

function renderAllViews() {
  updateDashboardCounts();
  renderDashboard();
  applyFilters();
  renderSavedPapers();
  renderLeaderboard();
}

// =============================================================================
// QUESTION PAPER IMAGE UPLOAD
// =============================================================================

function triggerFileInput() {
  document.getElementById('paper-image-input').click();
}

function handleImageSelection(event) {
  const file = event.target.files[0];
  if (!file) return;

  if (!file.type.startsWith('image/')) {
    alert('Please select a valid image file (JPEG, PNG, WebP).');
    return;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    selectedUploadImage = e.target.result;
    displayImagePreview(file.name, selectedUploadImage);
  };
  reader.readAsDataURL(file);
}

function displayImagePreview(filename, dataUrl) {
  const prompt = document.getElementById('dropzone-prompt');
  const preview = document.getElementById('dropzone-preview');
  const previewImg = document.getElementById('preview-img');
  const nameLabel = document.getElementById('preview-filename');

  previewImg.src = dataUrl;
  nameLabel.textContent = filename || 'Uploaded Paper Image';

  prompt.classList.add('hidden');
  preview.classList.remove('hidden');
}

function removeSelectedImage(e) {
  if (e) e.stopPropagation();
  selectedUploadImage = null;
  document.getElementById('paper-image-input').value = '';

  document.getElementById('dropzone-prompt').classList.remove('hidden');
  document.getElementById('dropzone-preview').classList.add('hidden');
}

function loadSamplePaperImage(type) {
  const isMid = type === 'mid';
  const name = isMid ? 'Mid-1 Assessment Paper' : 'Semester End Exam Paper';
  const sampleUrl = createOfficialPaperImage(
    isMid ? '21CS302' : '21CS301',
    isMid ? 'Mid-Term Assessment Paper' : 'Semester End Theory Paper',
    'CSE',
    5,
    isMid ? 'Mid-1' : 'Semester End',
    'November 2024',
    'R22'
  );

  selectedUploadImage = sampleUrl;
  displayImagePreview(`${type}_official_exam_scan.svg`, sampleUrl);
  showToast('Official SRU paper image loaded into upload form');
}

function handlePaperUpload(e) {
  e.preventDefault();

  if (!selectedUploadImage) {
    alert('Please select or upload a question paper image file.');
    return;
  }

  const subjectName = document.getElementById('up-subject-name').value.trim();
  const subjectCode = document.getElementById('up-subject-code').value.trim();
  const branch = document.getElementById('up-branch').value;
  const sem = parseInt(document.getElementById('up-sem').value, 10);
  const examType = document.getElementById('up-exam-type').value;
  const year = document.getElementById('up-year').value.trim() || '2024-2025';
  const monthYear = document.getElementById('up-month-year').value.trim() || 'November 2024';
  const regulation = document.getElementById('up-regulation').value.trim() || 'R22';

  const newPaper = {
    id: 'paper-user-' + Date.now(),
    subjectName: subjectName,
    subjectCode: subjectCode,
    branch: branch,
    semester: sem,
    examType: examType,
    year: year,
    monthYear: monthYear,
    regulation: regulation,
    uploaderName: currentUser ? currentUser.name : 'SRU Student',
    uploaderRoll: currentUser ? currentUser.roll : '2103A51001',
    uploadDate: new Date().toISOString().split('T')[0],
    imageUrl: selectedUploadImage,
    downloadsCount: 1
  };

  // Prepend to list
  papersList.unshift(newPaper);
  localStorage.setItem(STORAGE_KEYS.PAPERS, JSON.stringify(papersList));

  // Reward points
  if (currentUser) {
    currentUser.points = (currentUser.points || 0) + 50;
    currentUser.uploads = (currentUser.uploads || 0) + 1;
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(currentUser));
    updateUserInterface();
  }

  // Reset form
  document.getElementById('upload-paper-form').reset();
  removeSelectedImage();

  showToast('Question paper image uploaded successfully! (+50 Points)');
  navigateTo('papers');
}

// =============================================================================
// QUESTION PAPER IMAGE MODAL VIEWER
// =============================================================================

function openImageViewerModal(paperId) {
  const paper = papersList.find(p => p.id === paperId);
  if (!paper) return;

  const modal = document.getElementById('image-viewer-modal');
  const title = document.getElementById('modal-paper-title');
  const subtitle = document.getElementById('modal-paper-subtitle');
  const img = document.getElementById('modal-paper-image');
  const downloadBtn = document.getElementById('modal-download-btn');

  title.textContent = paper.subjectName;
  subtitle.textContent = `${paper.subjectCode} &bull; ${paper.branch} &bull; Semester ${paper.semester} &bull; ${paper.examType} (${paper.monthYear})`;
  img.src = paper.imageUrl;
  img.style.transform = 'scale(1)';
  currentZoom = 1.0;

  downloadBtn.href = paper.imageUrl;
  downloadBtn.download = `${paper.subjectCode}_${paper.branch}_${paper.examType}.png`;

  modal.classList.remove('hidden');
  document.body.style.overflow = 'hidden';
}

function closeImageViewerModal() {
  const modal = document.getElementById('image-viewer-modal');
  modal.classList.add('hidden');
  document.body.style.overflow = '';
}

function handleModalBackdropClick(e) {
  if (e.target.id === 'image-viewer-modal') {
    closeImageViewerModal();
  }
}

function zoomImage(factor) {
  currentZoom = Math.min(Math.max(0.5, currentZoom + factor), 3.0);
  const img = document.getElementById('modal-paper-image');
  if (img) {
    img.style.transform = `scale(${currentZoom})`;
  }
}

function resetZoom() {
  currentZoom = 1.0;
  const img = document.getElementById('modal-paper-image');
  if (img) {
    img.style.transform = 'scale(1)';
  }
}

// =============================================================================
// LEADERBOARD
// =============================================================================

function renderLeaderboard() {
  const tbody = document.getElementById('leaderboard-tbody');
  if (!tbody) return;

  const leaders = getInitialLeaderboard();

  // If current user has more uploads, update in leaderboard
  if (currentUser) {
    const existing = leaders.find(l => l.name === currentUser.name);
    if (existing) {
      existing.uploads = currentUser.uploads;
      existing.points = currentUser.points;
    }
  }

  leaders.sort((a, b) => b.points - a.points);

  tbody.innerHTML = leaders.map((l, idx) => `
    <tr>
      <td><span class="rank-pill ${idx === 0 ? 'gold' : ''}">#${idx + 1}</span></td>
      <td><strong>${l.name}</strong></td>
      <td>${l.branch}</td>
      <td>${l.uploads} Papers</td>
      <td><strong>${l.points} Pts</strong></td>
    </tr>
  `).join('');
}

// =============================================================================
// TOAST NOTIFICATIONS
// =============================================================================

function showToast(message) {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.textContent = message;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}
