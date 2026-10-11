import React, { useState } from 'react'
import { MapPin, Phone, Navigation } from 'lucide-react'

export default function SafeZonesExplorer() {
  const [activeFilter, setActiveFilter] = useState('all')

  const safeZones = [
    {
      id: 1,
      name: 'Central Women Police Station & 24x7 Help Desk',
      type: 'women_desk',
      typeLabel: '24x7 Women Desk',
      address: 'Sector 12, Police Lines (24x7 Armed Guard)',
      phone: '1091',
      coords: '28.6139, 77.2090'
    },
    {
      id: 2,
      name: 'City Police Headquarters & Emergency Command',
      type: 'police',
      typeLabel: 'Police Station',
      address: 'Ashoka Road, Civil Lines',
      phone: '112',
      coords: '28.6190, 77.2150'
    },
    {
      id: 3,
      name: 'Civil Hospital - 24x7 Trauma & Emergency Center',
      type: 'hospital',
      typeLabel: 'Hospital / Trauma',
      address: 'Medical Enclave, Ring Road',
      phone: '102',
      coords: '28.6050, 77.2200'
    },
    {
      id: 4,
      name: 'Central Metro Station CISF Security Booth',
      type: 'metro',
      typeLabel: 'Metro Security',
      address: 'Gate 2, CISF Women Security Unit',
      phone: '155370',
      coords: '28.6250, 77.2180'
    },
    {
      id: 5,
      name: 'One Stop Centre (Sakhi) for Women Distress Support',
      type: 'women_desk',
      typeLabel: '24x7 Women Desk',
      address: 'District Social Welfare Complex',
      phone: '181',
      coords: '28.6300, 77.2250'
    }
  ]

  const filtered = activeFilter === 'all' 
    ? safeZones 
    : safeZones.filter((z) => z.type === activeFilter)

  return (
    <section className="bg-white rounded-card p-6 lg:p-10 border-2 border-pink-border shadow-flat-card space-y-6">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h3 className="text-2xl lg:text-3xl font-extrabold text-ink-primary tracking-tight">
            Offline Tactical Safe Havens
          </h3>
          <p className="text-sm text-ink-secondary font-medium mt-1">
            Pre-cached emergency safety locations available offline with 1-tap navigation and emergency dialing.
          </p>
        </div>

        {/* Emergency Dial Chips */}
        <div className="flex flex-col sm:items-end gap-1">
          <div className="flex items-center gap-2">
            <a
              href="tel:112"
              className="flex items-center gap-1.5 px-4 py-2.5 rounded-btn bg-status-danger text-white text-xs font-bold shadow-sm hover:opacity-90 transition-all"
            >
              <Phone className="w-3.5 h-3.5" />
              <span>112 All Emergency</span>
            </a>
            <a
              href="tel:1091"
              className="flex items-center gap-1.5 px-4 py-2.5 rounded-btn bg-pink-primary text-white text-xs font-bold shadow-sm hover:bg-pink-pressed transition-all"
            >
              <Phone className="w-3.5 h-3.5" />
              <span>1091 Women Help</span>
            </a>
          </div>
          <span className="text-[11px] text-ink-secondary font-medium">
            *Emergency voice dialers trigger device cellular phone app
          </span>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex flex-wrap gap-2">
        {[
          { id: 'all', label: 'All Safe Havens' },
          { id: 'women_desk', label: '24x7 Women Desks' },
          { id: 'police', label: 'Police Stations' },
          { id: 'hospital', label: 'Hospitals' },
          { id: 'metro', label: 'Metro CISF Posts' },
        ].map((btn) => (
          <button
            key={btn.id}
            onClick={() => setActiveFilter(btn.id)}
            className={`px-4 py-2 rounded-full text-xs font-bold transition-all ${
              activeFilter === btn.id
                ? 'bg-pink-primary text-white shadow-sm'
                : 'bg-bg-app text-ink-secondary hover:bg-pink-soft border border-pink-border'
            }`}
          >
            {btn.label}
          </button>
        ))}
      </div>

      {/* List of Safe Zones */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {filtered.map((zone) => (
          <div
            key={zone.id}
            className="bg-bg-app rounded-2xl p-5 border border-pink-border hover:border-pink-primary shadow-sm flex flex-col justify-between space-y-4 transition-all"
          >
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-[10px] font-extrabold uppercase tracking-wider px-2 py-0.5 rounded-md bg-pink-soft text-pink-primary border border-pink-border">
                  {zone.typeLabel}
                </span>
                <span className="text-[11px] font-mono font-bold text-ink-secondary">
                  {zone.coords}
                </span>
              </div>

              <h4 className="text-base font-extrabold text-ink-primary leading-snug">
                {zone.name}
              </h4>
              <p className="text-xs text-ink-secondary font-medium mt-1 flex items-start gap-1.5">
                <MapPin className="w-4 h-4 text-pink-primary flex-shrink-0 mt-0.5" />
                <span>{zone.address}</span>
              </p>
            </div>

            <div className="flex items-center gap-2 pt-2 border-t border-pink-border">
              <a
                href={`tel:${zone.phone}`}
                className="flex-1 py-2.5 rounded-btn bg-status-safe text-white text-xs font-bold text-center flex items-center justify-center gap-1.5 shadow-sm hover:opacity-90 transition-all"
              >
                <Phone className="w-3.5 h-3.5" />
                <span>Call {zone.phone}</span>
              </a>

              <a
                href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(zone.coords)}`}
                target="_blank"
                rel="noreferrer"
                className="px-4 py-2.5 rounded-btn bg-white hover:bg-pink-soft border border-pink-border text-ink-primary text-xs font-bold flex items-center justify-center gap-1 transition-all"
              >
                <Navigation className="w-3.5 h-3.5 text-pink-primary" />
                <span>Maps</span>
              </a>
            </div>
          </div>
        ))}
      </div>

    </section>
  )
}
