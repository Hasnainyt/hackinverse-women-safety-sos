import React from 'react'
import { WifiOff, Binary } from 'lucide-react'

export default function OfflineArchitecture() {
  const steps = [
    {
      num: '01',
      title: 'Zero-Cloud Trigger',
      desc: 'Activated instantly via 1-tap centered SOS or 3 accelerometer shakes (>2.7g) with a 3-second false-alarm cancel window.'
    },
    {
      num: '02',
      title: 'Local Evidence & GPS',
      desc: 'MediaRecorder saves encrypted audio to app-private internal storage while FusedLocationProvider locks coordinates offline.'
    },
    {
      num: '03',
      title: '17-Byte BLE Packetizer',
      desc: 'Encodes magic (1B), version (1B), senderId (2B), timestamp (4B), lat (4B), lon (4B), and flags (1B) into a legacy 25B AD frame under UUID 0xFDE1.'
    },
    {
      num: '04',
      title: 'Multi-Hop Mesh & 2-Way ACK',
      desc: 'Nearby phones receive heads-up alarms even screen-off, relay across hops with TTL decrement, and transmit responder ACK beacons.'
    }
  ]

  return (
    <section className="bg-bg-plum text-white rounded-card p-6 lg:p-10 border-2 border-pink-border/20 shadow-flat-elevated space-y-8">
      
      {/* Section Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-xs font-black tracking-widest text-pink-lightened uppercase bg-bg-plumSurface px-3 py-1 rounded-full border border-pink-border/30">
            Engine & Architecture
          </span>
          <h3 className="text-3xl font-black text-white mt-2">
            Why 100% Offline P2P Beats Cloud SOS Apps
          </h3>
          <p className="text-sm text-pink-border/80 max-w-2xl mt-1 leading-relaxed">
            Traditional safety apps fail during network dead-zones, basements, or SIM jamming. Nari-Suraksha operates purely on device-level Bluetooth Low Energy radio.
          </p>
        </div>

        <div className="flex items-center gap-3 bg-bg-plumSurface px-4 py-3 rounded-2xl border border-pink-border/20">
          <WifiOff className="w-5 h-5 text-pink-lightened" />
          <div>
            <p className="text-xs font-bold text-white">0 Server Reliance</p>
            <p className="text-[10px] text-pink-border/70">Autonomous Edge Mesh</p>
          </div>
        </div>
      </div>

      {/* Step Cycle Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {steps.map((s) => (
          <div
            key={s.num}
            className="bg-bg-plumSurface border border-pink-border/20 rounded-2xl p-5 flex flex-col justify-between"
          >
            <div>
              <span className="text-2xl font-black text-pink-lightened">{s.num}</span>
              <h4 className="text-base font-extrabold text-white mt-2">{s.title}</h4>
              <p className="text-xs text-pink-border/70 font-medium mt-2 leading-relaxed">{s.desc}</p>
            </div>
            <div className="h-1 w-12 bg-pink-lightened rounded-full mt-4" />
          </div>
        ))}
      </div>

      {/* 17-Byte Packet Structure Breakdown */}
      <div className="bg-bg-plumSurface rounded-2xl p-5 border border-pink-border/20 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Binary className="w-4 h-4 text-pink-lightened" />
            <h5 className="text-xs font-extrabold uppercase tracking-wider text-pink-border">
              Legacy BLE Compliant Packet Spec (17 Bytes Total)
            </h5>
          </div>
          <span className="text-[10px] font-mono bg-bg-plum text-pink-lightened px-2.5 py-0.5 rounded border border-pink-border/30">
            16-bit UUID: 0xFDE1 (Legacy 25B AD Frame)
          </span>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-5 gap-2.5 text-center text-xs font-mono">
          <div className="bg-bg-plum p-3 rounded-xl border border-pink-border/20">
            <p className="text-pink-lightened font-bold">senderId</p>
            <p className="text-[10px] text-gray-400">Short (2 Bytes)</p>
          </div>
          <div className="bg-bg-plum p-3 rounded-xl border border-pink-border/20">
            <p className="text-pink-lightened font-bold">timestamp</p>
            <p className="text-[10px] text-gray-400">Epoch Int (4 Bytes)</p>
          </div>
          <div className="bg-bg-plum p-3 rounded-xl border border-pink-border/20">
            <p className="text-pink-lightened font-bold">latitude</p>
            <p className="text-[10px] text-gray-400">Float (4 Bytes)</p>
          </div>
          <div className="bg-bg-plum p-3 rounded-xl border border-pink-border/20">
            <p className="text-pink-lightened font-bold">longitude</p>
            <p className="text-[10px] text-gray-400">Float (4 Bytes)</p>
          </div>
          <div className="bg-bg-plum p-3 rounded-xl border border-pink-border/20 col-span-2 sm:col-span-1">
            <p className="text-pink-lightened font-bold">flags</p>
            <p className="text-[10px] text-gray-400">Bit0:SOS, Bit2:ACK</p>
          </div>
        </div>
      </div>

    </section>
  )
}
