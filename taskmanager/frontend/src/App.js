import { useState, useEffect, useRef, useCallback } from 'react';
import './App.css';

const API = 'http://localhost:8080/api';
const ASSOCIATE_ID = 'associate-1';

const STATUS_COLOR = {
  IN_PROGRESS: '#22c55e',
  PAUSED: '#f59e0b',
  PENDING: '#6366f1',
  COMPLETED: '#94a3b8',
};

const CATEGORY_ICON = {
  PLANNED: '📋',
  BOPIS: '📦',
  CUSTOMER: '🙋',
  ADHOC: '⚡',
  SAFETY: '🚨',
};

function PriorityBar({ score }) {
  const color = score >= 85 ? '#ef4444' : score >= 60 ? '#f59e0b' : '#6366f1';
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 4 }}>
      <div style={{ flex: 1, height: 6, background: '#1e293b', borderRadius: 3 }}>
        <div style={{ width: `${score}%`, height: '100%', background: color, borderRadius: 3, transition: 'width 0.5s ease' }} />
      </div>
      <span style={{ fontSize: 12, color, fontWeight: 700, minWidth: 32 }}>{score}</span>
    </div>
  );
}

function TaskCard({ task, onAction }) {
  const isActive = task.status === 'IN_PROGRESS';
  const isPaused = task.status === 'PAUSED';

  return (
    <div style={{
      background: '#1e293b', borderRadius: 12, padding: '16px', marginBottom: 10,
      border: isActive ? '1.5px solid #22c55e' : isPaused ? '1.5px solid #f59e0b' : '1.5px solid #334155',
      transition: 'all 0.3s ease',
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
            <span style={{ fontSize: 18 }}>{CATEGORY_ICON[task.category] || '📌'}</span>
            <span style={{ fontWeight: 700, fontSize: 15, color: '#f1f5f9' }}>{task.title}</span>
          </div>
          <p style={{ color: '#94a3b8', fontSize: 13, margin: '4px 0' }}>{task.description}</p>
          {task.aiReasoning && (
            <p style={{ color: '#818cf8', fontSize: 12, margin: '6px 0 0', fontStyle: 'italic' }}>
              🤖 {task.aiReasoning}
            </p>
          )}
          <PriorityBar score={task.priorityScore} />
        </div>
        <span style={{
          background: STATUS_COLOR[task.status] + '22', color: STATUS_COLOR[task.status],
          border: `1px solid ${STATUS_COLOR[task.status]}44`, borderRadius: 20,
          padding: '2px 10px', fontSize: 11, fontWeight: 700, marginLeft: 12, whiteSpace: 'nowrap',
        }}>
          {task.status.replace('_', ' ')}
        </span>
      </div>
      <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
        {task.status === 'PENDING' && (
          <button onClick={() => onAction(task.id, 'start')} style={btnStyle('#22c55e')}>▶ Start</button>
        )}
        {task.status === 'IN_PROGRESS' && (
          <>
            <button onClick={() => onAction(task.id, 'pause')} style={btnStyle('#f59e0b')}>⏸ Pause</button>
            <button onClick={() => onAction(task.id, 'complete')} style={btnStyle('#6366f1')}>✓ Complete</button>
          </>
        )}
        {task.status === 'PAUSED' && (
          <>
            <button onClick={() => onAction(task.id, 'resume')} style={btnStyle('#22c55e')}>▶ Resume</button>
            <button onClick={() => onAction(task.id, 'complete')} style={btnStyle('#6366f1')}>✓ Complete</button>
          </>
        )}
      </div>
    </div>
  );
}

function btnStyle(color) {
  return {
    background: color + '22', color, border: `1px solid ${color}44`,
    borderRadius: 8, padding: '6px 14px', fontSize: 13, fontWeight: 600, cursor: 'pointer',
  };
}

function AssociateView() {
  const [tasks, setTasks] = useState([]);
  const [connected, setConnected] = useState(false);
  const [flash, setFlash] = useState(null);
  const wsRef = useRef(null);

  const fetchTasks = useCallback(async () => {
    const res = await fetch(`${API}/tasks/${ASSOCIATE_ID}`);
    const data = await res.json();
    setTasks(data);
  }, []);

  useEffect(() => {
    fetchTasks();
    const ws = new WebSocket(`ws://localhost:8080/ws/tasks?associateId=${ASSOCIATE_ID}`);
    wsRef.current = ws;
    ws.onopen = () => setConnected(true);
    ws.onclose = () => setConnected(false);
    ws.onmessage = (e) => {
      const msg = JSON.parse(e.data);
      if (msg.type === 'TASK_UPDATE') {
        setTasks(msg.tasks);
        setFlash('Task list updated by AI prioritization');
        setTimeout(() => setFlash(null), 3000);
      }
    };
    return () => ws.close();
  }, [fetchTasks]);

  const handleAction = async (id, action) => {
    await fetch(`${API}/tasks/${id}/${action}`, { method: 'PUT' });
    fetchTasks();
  };

  const activeTasks = tasks.filter(t => t.status === 'IN_PROGRESS');
  const pausedTasks = tasks.filter(t => t.status === 'PAUSED');
  const pendingTasks = tasks.filter(t => t.status === 'PENDING');

  return (
    <div style={{ maxWidth: 420, margin: '0 auto', padding: '0 0 40px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div>
          <h2 style={{ margin: 0, color: '#f1f5f9', fontSize: 20 }}>My Tasks</h2>
          <p style={{ margin: 0, color: '#64748b', fontSize: 13 }}>Associate: Alex Chen</p>
        </div>
        <span style={{ display: 'flex', alignItems: 'center', gap: 6, color: connected ? '#22c55e' : '#ef4444', fontSize: 12, fontWeight: 600 }}>
          <span style={{ width: 8, height: 8, borderRadius: '50%', background: connected ? '#22c55e' : '#ef4444', display: 'inline-block' }} />
          {connected ? 'Live' : 'Offline'}
        </span>
      </div>

      {flash && (
        <div style={{ background: '#6366f122', border: '1px solid #6366f144', borderRadius: 10, padding: '10px 16px', color: '#818cf8', fontSize: 13, marginBottom: 16 }}>
          🤖 {flash}
        </div>
      )}

      {activeTasks.length > 0 && (
        <>
          <h3 style={{ color: '#22c55e', fontSize: 13, fontWeight: 700, textTransform: 'uppercase', letterSpacing: 1, marginBottom: 8 }}>In Progress</h3>
          {activeTasks.map(t => <TaskCard key={t.id} task={t} onAction={handleAction} />)}
        </>
      )}

      {pausedTasks.length > 0 && (
        <>
          <h3 style={{ color: '#f59e0b', fontSize: 13, fontWeight: 700, textTransform: 'uppercase', letterSpacing: 1, marginBottom: 8, marginTop: 16 }}>Paused</h3>
          {pausedTasks.map(t => <TaskCard key={t.id} task={t} onAction={handleAction} />)}
        </>
      )}

      {pendingTasks.length > 0 && (
        <>
          <h3 style={{ color: '#6366f1', fontSize: 13, fontWeight: 700, textTransform: 'uppercase', letterSpacing: 1, marginBottom: 8, marginTop: 16 }}>Queue</h3>
          {pendingTasks.map(t => <TaskCard key={t.id} task={t} onAction={handleAction} />)}
        </>
      )}

      {tasks.length === 0 && (
        <div style={{ textAlign: 'center', color: '#475569', marginTop: 60 }}>
          <p style={{ fontSize: 40 }}>✓</p>
          <p>All clear! No active tasks.</p>
        </div>
      )}
    </div>
  );
}

const QUICK_SCENARIOS = [
  { label: '🚨 Milk Spill Aisle 3', title: 'Milk Spill - Aisle 3', description: 'Safety hazard - liquid spill, slip risk for customers', category: 'SAFETY' },
  { label: '📦 New BOPIS Order', title: 'BOPIS Order #8844', description: 'Online pickup order ready for item collection', category: 'BOPIS' },
  { label: '🙋 Customer Needs Help', title: 'Customer Assistance - Dairy', description: 'Customer requesting help locating dairy alternatives', category: 'CUSTOMER' },
  { label: '📋 Restock Frozen Aisle', title: 'Restock Frozen Foods - Aisle 9', description: 'Low stock on frozen meals, needs replenishment', category: 'PLANNED' },
];

function ManagerView() {
  const [form, setForm] = useState({ title: '', description: '', category: 'ADHOC' });
  const [sending, setSending] = useState(false);
  const [sent, setSent] = useState(null);

  const sendTask = async (taskData) => {
    setSending(true);
    try {
      const res = await fetch(`${API}/tasks`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...taskData, associateId: ASSOCIATE_ID }),
      });
      const task = await res.json();
      setSent(`Sent! AI scored priority: ${task.priorityScore}/100`);
      setForm({ title: '', description: '', category: 'ADHOC' });
      setTimeout(() => setSent(null), 4000);
    } finally {
      setSending(false);
    }
  };

  return (
    <div style={{ maxWidth: 420, margin: '0 auto' }}>
      <div style={{ marginBottom: 20 }}>
        <h2 style={{ margin: 0, color: '#f1f5f9', fontSize: 20 }}>Manager Console</h2>
        <p style={{ margin: 0, color: '#64748b', fontSize: 13 }}>Assign tasks to Alex Chen</p>
      </div>

      <div style={{ marginBottom: 24 }}>
        <h3 style={{ color: '#94a3b8', fontSize: 13, fontWeight: 700, textTransform: 'uppercase', letterSpacing: 1, marginBottom: 12 }}>Quick Scenarios</h3>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
          {QUICK_SCENARIOS.map(s => (
            <button key={s.label} onClick={() => sendTask(s)} disabled={sending}
              style={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 10, padding: '12px', color: '#f1f5f9', fontSize: 13, fontWeight: 600, cursor: 'pointer', textAlign: 'left', lineHeight: 1.4 }}>
              {s.label}
            </button>
          ))}
        </div>
      </div>

      <div style={{ background: '#1e293b', borderRadius: 12, padding: 20, border: '1px solid #334155' }}>
        <h3 style={{ color: '#94a3b8', fontSize: 13, fontWeight: 700, textTransform: 'uppercase', letterSpacing: 1, marginBottom: 16, marginTop: 0 }}>Custom Task</h3>
        <label style={labelStyle}>Task Title</label>
        <input value={form.title} onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
          placeholder="e.g. Clean Checkout Lane 4" style={inputStyle} />
        <label style={labelStyle}>Description</label>
        <textarea value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
          placeholder="Details about the task..." rows={3} style={{ ...inputStyle, resize: 'vertical' }} />
        <label style={labelStyle}>Category</label>
        <select value={form.category} onChange={e => setForm(f => ({ ...f, category: e.target.value }))} style={inputStyle}>
          <option value="ADHOC">Ad-hoc</option>
          <option value="SAFETY">Safety</option>
          <option value="BOPIS">BOPIS</option>
          <option value="CUSTOMER">Customer</option>
          <option value="PLANNED">Planned</option>
        </select>
        <button onClick={() => sendTask(form)} disabled={sending || !form.title}
          style={{ width: '100%', marginTop: 8, padding: '12px', background: sending || !form.title ? '#334155' : '#6366f1', color: '#fff', border: 'none', borderRadius: 10, fontSize: 15, fontWeight: 700, cursor: form.title ? 'pointer' : 'not-allowed' }}>
          {sending ? 'Sending to AI...' : '⚡ Assign Task'}
        </button>
      </div>

      {sent && (
        <div style={{ background: '#22c55e22', border: '1px solid #22c55e44', borderRadius: 10, padding: '12px 16px', color: '#22c55e', fontSize: 14, fontWeight: 600, marginTop: 12 }}>
          ✓ {sent}
        </div>
      )}
    </div>
  );
}

const labelStyle = { display: 'block', color: '#94a3b8', fontSize: 12, fontWeight: 600, marginBottom: 6, marginTop: 14 };
const inputStyle = { width: '100%', boxSizing: 'border-box', background: '#0f172a', border: '1px solid #334155', borderRadius: 8, padding: '10px 12px', color: '#f1f5f9', fontSize: 14, outline: 'none' };

export default function App() {
  const [view, setView] = useState('associate');
  return (
    <div style={{ minHeight: '100vh', background: '#0f172a', color: '#f1f5f9', fontFamily: '-apple-system, BlinkMacSystemFont, sans-serif' }}>
      <div style={{ background: '#1e293b', borderBottom: '1px solid #334155', padding: '0 20px' }}>
        <div style={{ maxWidth: 420, margin: '0 auto', display: 'flex', alignItems: 'center', justifyContent: 'space-between', height: 56 }}>
          <span style={{ fontWeight: 800, fontSize: 16, color: '#6366f1' }}>🛒 StoreIQ</span>
          <div style={{ display: 'flex', gap: 4, background: '#0f172a', borderRadius: 10, padding: 4 }}>
            {['associate', 'manager'].map(v => (
              <button key={v} onClick={() => setView(v)}
                style={{ padding: '6px 16px', borderRadius: 7, border: 'none', background: view === v ? '#6366f1' : 'transparent', color: view === v ? '#fff' : '#64748b', fontSize: 13, fontWeight: 600, cursor: 'pointer', textTransform: 'capitalize' }}>
                {v === 'associate' ? '👤 Associate' : '👔 Manager'}
              </button>
            ))}
          </div>
        </div>
      </div>
      <div style={{ padding: '24px 20px' }}>
        {view === 'associate' ? <AssociateView /> : <ManagerView />}
      </div>
    </div>
  );
}
