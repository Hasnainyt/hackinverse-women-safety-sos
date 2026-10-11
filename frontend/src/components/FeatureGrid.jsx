import React from 'react'
import { AlertCircle, MapPin, Radio, Users, Calculator, ShieldCheck, ArrowRight } from 'lucide-react'

export default function FeatureGrid({ onSelectFeature }) {
  const coreFeatures = [
    {
      id: 'emergency',
      title: 'Emergency',
      subtitle: 'Quick help when you need it most.',
      description: '1-Tap centered SOS button or 3 rapid accelerometer shakes (>2.7g) triggers a 3-second countdown with haptic feedback.',
      icon: AlertCircle,
      badge: 'Multi-Trigger',
      iconBg: 'bg-status-danger text-white'
    },
    {
      id: 'detection',
      title: 'Detection',
      subtitle: 'Capture location & evidence offline.',
      description: 'Locks GPS coordinates without internet and records microphone evidence directly to protected app-private storage.',
      icon: MapPin,
      badge: 'Offline GPS + Mic',
      iconBg: 'bg-pink-primary text-white'
    },
    {
      id: 'broadcast',
      title: 'Broadcast',
      subtitle: 'Live alert to nearby phones offline.',
      description: 'Encodes 17-byte binary packet under Service UUID 0xFDE1 and broadcasts over BLE mesh with multi-hop flood routing.',
      icon: Radio,
      badge: 'Flooding BLE Mesh',
      iconBg: 'bg-status-guardian text-white'
    },
    {
      id: 'community',
      title: 'Community',
      subtitle: 'Autonomous guardian responder network.',
      description: 'Bystander phones receive heads-up alarms even screen-off, with estimated distance (<3m, 3-10m) and 2-way ACK return.',
      icon: Users,
      badge: '2-Way Responder ACK',
      iconBg: 'bg-status-safe text-white'
    }
  ]

  const extraFeatures = [
    {
      id: 'decoy',
      title: 'Decoy Calculator Disguise',
      subtitle: 'Hidden in Plain Sight',
      description: 'A fully working arithmetic calculator in neutral dark gray. Entering secret PIN followed by = triggers silent SOS without tipping off an attacker.',
      icon: Calculator,
      tag: 'Neutral Disguise'
    },
    {
      id: 'safezones',
      title: 'Offline Tactical Radar & Safe Havens',
      subtitle: 'Verified Safety Havens',
      description: 'Pre-cached directory with concentric radar rings (500m, 1km, 2km, 5km) for 24x7 Women Police Desks and Trauma Centers with 1-tap call.',
      icon: ShieldCheck,
      tag: 'Offline Radar'
    }
  ]

  return (
    <div className="space-y-6">
      
      {/* Section Header */}
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-2xl lg:text-3xl font-extrabold text-ink-primary tracking-tight">
            How Nari-Suraksha Protects You
          </h3>
          <p className="text-sm font-medium text-ink-secondary mt-1">
            A stress-proof 4-stage safety cycle engineered directly into every device.
          </p>
        </div>
        <span className="hidden sm:inline-flex text-xs font-bold text-pink-primary bg-pink-soft px-3 py-1 rounded-full border border-pink-border">
          4-Stage Protection Cycle
        </span>
      </div>

      {/* 4 Core Cards with Solid Colors & 24px Card Radius */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {coreFeatures.map((f) => {
          const Icon = f.icon
          return (
            <div
              key={f.id}
              onClick={() => onSelectFeature && onSelectFeature(f.id)}
              className="bg-white rounded-card p-6 border-2 border-pink-border hover:border-pink-primary shadow-flat-card hover:shadow-flat-elevated transition-all flex flex-col justify-between cursor-pointer"
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <div className={`w-12 h-12 rounded-2xl ${f.iconBg} flex items-center justify-center shadow-sm`}>
                    <Icon className="w-6 h-6 stroke-[2.2]" />
                  </div>
                  <span className="text-[10px] font-extrabold text-pink-primary bg-pink-soft px-2.5 py-0.5 rounded-full border border-pink-border">
                    {f.badge}
                  </span>
                </div>

                <h4 className="text-xl font-extrabold text-ink-primary">
                  {f.title}
                </h4>
                <p className="text-xs font-bold text-pink-primary mt-1">
                  {f.subtitle}
                </p>
                <p className="text-xs text-ink-secondary font-medium mt-2 leading-relaxed">
                  {f.description}
                </p>
              </div>

              <div className="mt-5 pt-3 border-t border-pink-border flex items-center justify-between text-pink-primary">
                <span className="text-xs font-bold">Inspect Simulation</span>
                <div className="w-7 h-7 rounded-full bg-pink-soft flex items-center justify-center text-pink-primary">
                  <ArrowRight className="w-3.5 h-3.5" />
                </div>
              </div>
            </div>
          )
        })}
      </div>

      {/* 2 Extra Features */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5 pt-1">
        {extraFeatures.map((f) => {
          const Icon = f.icon
          return (
            <div
              key={f.id}
              onClick={() => onSelectFeature && onSelectFeature(f.id)}
              className="bg-white rounded-card p-6 border-2 border-pink-border hover:border-pink-primary shadow-flat-card flex items-center gap-5 cursor-pointer transition-all"
            >
              <div className="w-14 h-14 rounded-2xl bg-pink-soft text-pink-primary flex-shrink-0 flex items-center justify-center border border-pink-border">
                <Icon className="w-7 h-7 stroke-[2]" />
              </div>
              <div className="flex-1">
                <div className="flex items-center gap-2">
                  <h4 className="text-base font-extrabold text-ink-primary">
                    {f.title}
                  </h4>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-pink-soft text-pink-primary border border-pink-border">
                    {f.tag}
                  </span>
                </div>
                <p className="text-xs text-ink-secondary font-medium mt-1 leading-relaxed">
                  {f.description}
                </p>
              </div>
              <ArrowRight className="w-5 h-5 text-pink-primary" />
            </div>
          )
        })}
      </div>

    </div>
  )
}
