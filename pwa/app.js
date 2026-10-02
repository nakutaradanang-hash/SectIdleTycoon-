/**
 * Sect Idle — Xianxia Immortal Sect Management PWA Web Game Engine
 * Includes PWA installer handlers, offline persistence, audio synthesis, and game ticks.
 */

// =============================================================================
// 1. PWA INSTALLER & SERVICE WORKER MANAGEMENT
// =============================================================================
let deferredPrompt = null;

const installPwaBtn = document.getElementById('installPwaBtn');
const pwaStatusBadge = document.getElementById('pwaStatusBadge');
const offlineStatusIndicator = document.getElementById('offlineStatusIndicator');

// Register Service Worker for PWA
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js')
      .then((reg) => {
        console.log('[PWA] Service Worker registered successfully:', reg.scope);
      })
      .catch((err) => {
        console.error('[PWA] Service Worker registration failed:', err);
      });
  });
}

// Catch PWA Install Prompt Event
window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  deferredPrompt = e;
  if (installPwaBtn) {
    installPwaBtn.style.display = 'inline-flex';
    installPwaBtn.classList.add('pulse');
  }
  if (pwaStatusBadge) {
    pwaStatusBadge.textContent = '⚡ Ready to Install PWA';
    pwaStatusBadge.className = 'badge badge-ready';
  }
});

// Function to trigger PWA Installation Prompt
window.installPWA = function() {
  if (!deferredPrompt) {
    alert('PWA App is already installed or your browser is running in standalone mode.');
    return;
  }

  deferredPrompt.prompt();
  deferredPrompt.userChoice.then((choiceResult) => {
    if (choiceResult.outcome === 'accepted') {
      console.log('[PWA] User accepted PWA installation');
      if (installPwaBtn) installPwaBtn.style.display = 'none';
      if (pwaStatusBadge) {
        pwaStatusBadge.textContent = '✅ PWA Installed';
        pwaStatusBadge.className = 'badge badge-installed';
      }
    } else {
      console.log('[PWA] User dismissed PWA installation');
    }
    deferredPrompt = null;
  });
};

// Listen for PWA App Installation
window.addEventListener('appinstalled', () => {
  console.log('[PWA] Sect Idle PWA was installed successfully!');
  if (installPwaBtn) installPwaBtn.style.display = 'none';
  if (pwaStatusBadge) {
    pwaStatusBadge.textContent = '✅ PWA Standalone Mode';
    pwaStatusBadge.className = 'badge badge-installed';
  }
});

// Track Online/Offline Status
function updateOnlineStatus() {
  if (offlineStatusIndicator) {
    if (navigator.onLine) {
      offlineStatusIndicator.textContent = '🟢 Online';
      offlineStatusIndicator.className = 'status-online';
    } else {
      offlineStatusIndicator.textContent = '⚡ Offline Play Active';
      offlineStatusIndicator.className = 'status-offline';
    }
  }
}
window.addEventListener('online', updateOnlineStatus);
window.addEventListener('offline', updateOnlineStatus);
updateOnlineStatus();

// =============================================================================
// 2. GAME ENGINE & STATE MANAGEMENT
// =============================================================================

const REALMS = [
  "Mortal (凡人)",
  "Qi Refining (炼气期)",
  "Foundation Establishment (筑基期)",
  "Core Formation (金丹期)",
  "Nascent Soul (元婴期)",
  "Soul Transformation (化神期)",
  "Void Refinement (合体期)",
  "Body Integration (渡劫期)",
  "Tribulation Transcendence (大乘期)",
  "True Immortal (真仙)",
  "Golden Immortal (金仙)",
  "Primordial Immortal (太乙金仙)",
  "Dao Ancestor (道祖)",
  "Heavenly Dao (天道圣人)"
];

const INITIAL_STATE = {
  spiritStones: 500,
  spiritHerbs: 100,
  spiritOres: 50,
  sectPower: 1200,
  dayCount: 1,
  disciples: [
    { id: 1, name: "Li Wei (李魏)", realmIndex: 1, exp: 45, maxExp: 100, task: "Cultivation", talent: "Heavenly Spirit (天灵根)" },
    { id: 2, name: "Feng Yan (风炎)", realmIndex: 0, exp: 80, maxExp: 100, task: "Mining", talent: "Body Refining (体修)" },
    { id: 3, name: "Su Lin (苏林)", realmIndex: 1, exp: 20, maxExp: 100, task: "Alchemy", talent: "Spirit Herb (药师)" }
  ],
  buildings: {
    mainHall: 1,
    spiritMine: 1,
    spiritGarden: 1,
    alchemyPavilion: 1
  },
  soundEnabled: true
};

let gameState = JSON.parse(localStorage.getItem('sect_idle_pwa_save')) || INITIAL_STATE;

function saveGameState() {
  localStorage.setItem('sect_idle_pwa_save', JSON.stringify(gameState));
}

// =============================================================================
// 3. WEB AUDIO SYNTHESIZER (XIANXIA SFX)
// =============================================================================
let audioCtx = null;

function playSfx(type) {
  if (!gameState.soundEnabled) return;
  try {
    if (!audioCtx) {
      audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }
    if (audioCtx.state === 'suspended') {
      audioCtx.resume();
    }

    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    const now = audioCtx.currentTime;

    if (type === 'breakthrough') {
      // Heavenly chime upward sweep
      osc.type = 'sine';
      osc.frequency.setValueAtTime(440, now);
      osc.frequency.exponentialRampToValueAtTime(1760, now + 0.6);
      gain.gain.setValueAtTime(0.3, now);
      gain.gain.exponentialRampToValueAtTime(0.001, now + 0.6);
      osc.start(now);
      osc.stop(now + 0.6);
    } else if (type === 'harvest') {
      // Resource harvest ping
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(523.25, now); // C5
      osc.frequency.setValueAtTime(659.25, now + 0.08); // E5
      gain.gain.setValueAtTime(0.2, now);
      gain.gain.exponentialRampToValueAtTime(0.001, now + 0.25);
      osc.start(now);
      osc.stop(now + 0.25);
    } else if (type === 'recruit') {
      // Disciple recruit gong chord
      osc.type = 'sine';
      osc.frequency.setValueAtTime(329.63, now); // E4
      gain.gain.setValueAtTime(0.4, now);
      gain.gain.exponentialRampToValueAtTime(0.001, now + 0.8);
      osc.start(now);
      osc.stop(now + 0.8);
    }
  } catch (err) {
    console.warn('Audio synthesis warning:', err);
  }
}

// =============================================================================
// 4. GAME LOOP & ACTION HANDLERS
// =============================================================================

function gameTick() {
  gameState.dayCount++;

  // Resource Production from Buildings & Disciples
  const miningDisciples = gameState.disciples.filter(d => d.task === 'Mining').length;
  const farmingDisciples = gameState.disciples.filter(d => d.task === 'Farming').length;
  const cultivationDisciples = gameState.disciples.filter(d => d.task === 'Cultivation').length;

  gameState.spiritStones += 10 * gameState.buildings.mainHall + miningDisciples * 5;
  gameState.spiritOres += 2 * gameState.buildings.spiritMine + miningDisciples * 3;
  gameState.spiritHerbs += 2 * gameState.buildings.spiritGarden + farmingDisciples * 3;

  // Cultivation EXP Gain
  gameState.disciples.forEach(d => {
    if (d.task === 'Cultivation') {
      d.exp += 15;
    } else {
      d.exp += 5;
    }

    if (d.exp >= d.maxExp && d.realmIndex < REALMS.length - 1) {
      d.realmIndex++;
      d.exp = 0;
      d.maxExp = Math.floor(d.maxExp * 1.8);
      addLog(`✨ Disciple ${d.name} achieved Realm Breakthrough! New Realm: ${REALMS[d.realmIndex]}`);
      playSfx('breakthrough');
    }
  });

  // Calculate Total Sect Power
  let totalPower = 0;
  gameState.disciples.forEach(d => {
    totalPower += (d.realmIndex + 1) * 350 + Math.floor(d.exp * 2);
  });
  totalPower += gameState.buildings.mainHall * 500;
  gameState.sectPower = totalPower;

  saveGameState();
  renderUI();
}

function addLog(msg) {
  const logContainer = document.getElementById('gameLog');
  if (logContainer) {
    const entry = document.createElement('div');
    entry.className = 'log-entry';
    entry.innerHTML = `<span class="log-time">[Day ${gameState.dayCount}]</span> ${msg}`;
    logContainer.prepend(entry);
    if (logContainer.children.length > 25) {
      logContainer.removeChild(logContainer.lastChild);
    }
  }
}

function renderUI() {
  document.getElementById('resStones').textContent = Math.floor(gameState.spiritStones);
  document.getElementById('resHerbs').textContent = Math.floor(gameState.spiritHerbs);
  document.getElementById('resOres').textContent = Math.floor(gameState.spiritOres);
  document.getElementById('resSectPower').textContent = Math.floor(gameState.sectPower);
  document.getElementById('valDay').textContent = gameState.dayCount;

  // Render Disciples
  const discipleList = document.getElementById('discipleList');
  if (discipleList) {
    discipleList.innerHTML = gameState.disciples.map(d => `
      <div class="disciple-card">
        <div class="disciple-header">
          <span class="disciple-name">${d.name}</span>
          <span class="disciple-realm">${REALMS[d.realmIndex]}</span>
        </div>
        <div class="disciple-body">
          <div><small>Talent:</small> ${d.talent}</div>
          <div><small>Task:</small> <span class="task-badge">${d.task}</span></div>
          <div class="progress-bar-bg">
            <div class="progress-bar-fill" style="width: ${(d.exp / d.maxExp * 100).toFixed(1)}%"></div>
          </div>
          <div class="exp-text"><small>EXP: ${d.exp}/${d.maxExp}</small></div>
        </div>
      </div>
    `).join('');
  }
}

// User Actions
window.recruitDisciple = function() {
  const cost = 200;
  if (gameState.spiritStones < cost) {
    alert('Not enough Spirit Stones! Required: ' + cost);
    return;
  }
  gameState.spiritStones -= cost;

  const familyNames = ["Zhao", "Qian", "Sun", "Li", "Zhou", "Wu", "Zheng", "Wang"];
  const givenNames = ["Tian", "Yun", "Long", "Feng", "Xue", "Ming", "Jian", "Yu"];
  const talents = ["Heavenly Spirit (天灵根)", "Sword Immortal (剑仙)", "Alchemy Genius (丹圣)", "Body Refining (体修)", "Dual Cultivation (双修)"];

  const name = familyNames[Math.floor(Math.random() * familyNames.length)] + " " + givenNames[Math.floor(Math.random() * givenNames.length)];
  const newDisciple = {
    id: Date.now(),
    name: name,
    realmIndex: 0,
    exp: 0,
    maxExp: 100,
    task: "Cultivation",
    talent: talents[Math.floor(Math.random() * talents.length)]
  };

  gameState.disciples.push(newDisciple);
  playSfx('recruit');
  addLog(`⚔️ Recruited new disciple: ${name} (${newDisciple.talent})`);
  saveGameState();
  renderUI();
};

window.upgradeBuilding = function(type) {
  const cost = (gameState.buildings[type] || 1) * 300;
  if (gameState.spiritStones < cost) {
    alert('Not enough Spirit Stones! Required: ' + cost);
    return;
  }
  gameState.spiritStones -= cost;
  gameState.buildings[type] = (gameState.buildings[type] || 1) + 1;
  playSfx('harvest');
  addLog(`🏛️ Upgraded ${type} to Level ${gameState.buildings[type]}`);
  saveGameState();
  renderUI();
};

window.toggleSound = function() {
  gameState.soundEnabled = !gameState.soundEnabled;
  const btn = document.getElementById('soundBtn');
  if (btn) btn.textContent = gameState.soundEnabled ? '🔊 Sound: ON' : '🔇 Sound: OFF';
  saveGameState();
};

// Initialize Game Loop
window.addEventListener('DOMContentLoaded', () => {
  renderUI();
  addLog('🌸 Welcome to Xianxia Immortal Sect Management PWA!');
  setInterval(gameTick, 2000); // Game tick every 2 seconds
});
