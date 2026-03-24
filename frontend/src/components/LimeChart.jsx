/**
 * LimeChart.jsx
 * =============
 * Interactive horizontal bar chart for LIME word-importance explanations.
 * Uses Recharts (already in package.json).
 *
 * Props:
 *   explanations   [{word, weight}, ...]   — LIME features sorted by |weight|
 *   isLoading      bool                    — shows skeleton while re-fetching
 */
import React from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceLine,
  ResponsiveContainer,
  Cell,
} from 'recharts';

// ── Custom Tooltip ─────────────────────────────────────────────
const CustomTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  const { word, weight } = payload[0].payload;
  const isSuspicious = weight > 0;
  return (
    <div className="lime-tooltip">
      <strong className="lime-tooltip-word">"{word}"</strong>
      <div className={`lime-tooltip-weight ${isSuspicious ? 'suspicious' : 'safe'}`}>
        {isSuspicious ? '🚩 Suspicious' : '✅ Safe'}&nbsp;&nbsp;
        <span>{weight > 0 ? '+' : ''}{weight.toFixed(4)}</span>
      </div>
      <p className="lime-tooltip-hint">
        {isSuspicious
          ? 'Pushes prediction toward FAKE'
          : 'Pushes prediction toward REAL'
        }
      </p>
    </div>
  );
};

// ── Skeleton loader ─────────────────────────────────────────────
const ChartSkeleton = ({ rows = 10 }) => (
  <div className="lime-skeleton">
    {Array.from({ length: rows }).map((_, i) => (
      <div key={i} className="lime-skeleton-row">
        <div className="lime-skeleton-label" style={{ width: `${40 + Math.random() * 40}%` }} />
        <div className="lime-skeleton-bar" style={{ width: `${20 + Math.random() * 60}%` }} />
      </div>
    ))}
  </div>
);

// ── Main Chart ──────────────────────────────────────────────────
const LimeChart = ({ explanations = [], isLoading = false }) => {
  if (isLoading) return <ChartSkeleton rows={10} />;
  if (!explanations || explanations.length === 0) {
    return (
      <div className="lime-empty">
        <span className="lime-empty-icon">🔍</span>
        <p>No explanation features available.</p>
      </div>
    );
  }

  // Recharts renders top-to-bottom; reverse so highest importance is on top
  const data = [...explanations].reverse();

  return (
    <div className="lime-chart-container">
      <ResponsiveContainer width="100%" height={Math.max(200, data.length * 36)}>
        <BarChart
          data={data}
          layout="vertical"
          margin={{ top: 4, right: 60, left: 20, bottom: 4 }}
          barCategoryGap="20%"
        >
          <CartesianGrid strokeDasharray="3 3" stroke="var(--border-color, rgba(255,255,255,0.08))" horizontal={false} />
          <XAxis
            type="number"
            tickFormatter={v => v.toFixed(2)}
            tick={{ fill: 'var(--text-muted, #888)', fontSize: 11 }}
            axisLine={{ stroke: 'var(--border-color, rgba(255,255,255,0.1))' }}
            tickLine={false}
            domain={['auto', 'auto']}
          />
          <YAxis
            type="category"
            dataKey="word"
            width={110}
            tick={{ fill: 'var(--text-secondary, #ccc)', fontSize: 12, fontWeight: 500 }}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
          <ReferenceLine x={0} stroke="var(--text-muted, #666)" strokeDasharray="4 2" />
          <Bar dataKey="weight" radius={[0, 4, 4, 0]} maxBarSize={22}>
            {data.map((entry, index) => (
              <Cell
                key={`cell-${index}`}
                fill={entry.weight > 0
                  ? 'var(--color-danger, #ef4444)'
                  : 'var(--color-success, #22c55e)'
                }
                fillOpacity={0.85}
              />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>

      {/* Legend */}
      <div className="lime-legend">
        <span className="lime-legend-item">
          <span className="lime-legend-dot suspicious" /> Suspicious (→ FAKE)
        </span>
        <span className="lime-legend-item">
          <span className="lime-legend-dot safe" /> Safe (→ REAL)
        </span>
      </div>
    </div>
  );
};

export default LimeChart;
