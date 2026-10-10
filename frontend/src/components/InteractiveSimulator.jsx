import React, { useState, useEffect } from 'react'
import {
  AlertCircle,
  Radio,
  Shield,
  CheckCircle,
  Volume2,
  Flashlight,
  RefreshCw,
  Smartphone,
  MapPin,
  Signal,
  ArrowRight,
  ArrowLeft,
  XCircle,
  WifiOff,
  Navigation,
  Activity
} from 'lucide-react'
import { useI18n } from '../i18n'

export default function InteractiveSimulator() {
  const i18n = useI18n()
  const t = i18n.simulator

  // Simulation State
  // 'idle' | 'countdown' | 'hop1_sending' | 'hop1_reached' | 'hop2_sending' | 'hop2_reached' | 'ack_sending' | 'ack_delivered'
  const [stage, setStage] = useState('idle')
  const [countdown, setCountdown] = useState(3)
  
  // Relay B Configuration
  const [isRelayEnabled, setIsRelayEnabled] = useState(true)

  // Phone A state
  const [responderCount, setResponderCount] = useState(0)

  // Phone C state
  const [isAckSent, setIsAckSent] = useState(false)
  const [isSirenOn, setIsSirenOn] = useState(false)
  const [isTorchOn, setIsTorchOn] = useState(false)

  // Countdown timer for Phone A
  useEffect(() => {
    let timer
    if (stage === 'countdown') {
      if (countdown > 0) {
        timer = setTimeout(() => setCountdown((c) => c - 1), 1000)
      } else {
        // Countdown finished, begin Hop 1 broadcast from Phone A to Phone B
        setStage('hop1_sending')
      }
    }
    return () => clearTimeout(timer)
  }, [stage, countdown])

  // Packet animation pipeline
  useEffect(() => {
    let timer
    if (stage === 'hop1_sending') {
      // Packet travels from Phone A -> Phone B (1.2s)
      timer = setTimeout(() => {
        setStage('hop1_reached')
      }, 1200)
    } else if (stage === 'hop1_reached') {
      if (isRelayEnabled) {
        // Relay B re-broadcasts packet to Phone C
        timer = setTimeout(() => {
          setStage('hop2_sending')
        }, 900)
      } else {
        // Relay B is disabled: packet is dropped at B, cannot reach C!
        // Stays in 'hop1_reached' with dropped message
      }
    } else if (stage === 'hop2_sending') {
      // Packet travels from Phone B -> Phone C (1.2s)
      timer = setTimeout(() => {
        setStage('hop2_reached')
      }, 1200)
    } else if (stage === 'ack_sending') {
      // ACK travels backwards: C -> B -> A (1.4s)
      timer = setTimeout(() => {
        setStage('ack_delivered')
        setResponderCount(1)
      }, 1400)
    }
    return () => clearTimeout(timer)
  }, [stage, isRelayEnabled])

  const handleStartSos = () => {
    setCountdown(3)
    setStage('countdown')
    setResponderCount(0)
    setIsAckSent(false)
    setIsSirenOn(false)
    setIsTorchOn(false)
  }

  const handleCancel = () => {
    setStage('idle')
    setCountdown(3)
    setResponderCount(0)
    setIsAckSent(false)
  }

  const handleReset = () => {
    setStage('idle')
    setCountdown(3)
    setResponderCount(0)
    setIsAckSent(false)
    setIsSirenOn(false)
    setIsTorchOn(false)
  }

  const handleSendAck = () => {
    setIsAckSent(true)
    setStage('ack_sending')
  }

  const isBroadcasting = ['hop1_sending', 'hop1_reached', 'hop2_sending', 'hop2_reached', 'ack_sending', 'ack_delivered'].includes(stage)
  const isPacketAtB = ['hop1_reached', 'hop2_sending', 'hop2_reached', 'ack_sending', 'ack_delivered'].includes(stage)
  const isPacketAtC = ['hop2_reached', 'ack_sending', 'ack_delivered'].includes(stage)

  return (
    <section className="bg-white rounded-card p-6 lg:p-10 border-2 border-pink-border shadow-flat-card space-y-8">
      
      {/* Centerpiece Header with Projector-Ready Type */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 pb-6 border-b border-pink-border">
        <div>
          <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-pink-soft text-pink-primary border border-pink-border text-xs font-bold uppercase tracking-wider mb-2">
            <Radio className="w-3.5 h-3.5" />
            <span>{t.badge}</span>
          </div>
          <h2 className="text-3xl lg:text-4xl font-extrabold text-ink-primary tracking-tight">
            {t.title}
          </h2>
          <p className="text-base text-ink-secondary font-medium mt-1.5 max-w-2xl leading-relaxed">
            {t.subtitle}
          </p>
        </div>

        {/* Global Simulation Actions */}
        <div className="flex flex-wrap items-center gap-3">
          <button
            onClick={() => setIsRelayEnabled(!isRelayEnabled)}
            className={`px-5 py-3 rounded-btn text-sm font-bold border-2 transition-all flex items-center gap-2 shadow-sm ${
              isRelayEnabled
                ? 'bg-white border-pink-primary text-pink-primary hover:bg-pink-soft'
                : 'bg-status-amber text-white border-status-amber hover:opacity-90'
            }`}
          >
            {isRelayEnabled ? (
              <>
                <WifiOff className="w-4 h-4" />
                <span>{t.disableRelayBtn}</span>
              </>
            ) : (
              <>
                <Radio className="w-4 h-4" />
                <span>{t.enableRelayBtn}</span>
              </>
            )}
          </button>

          <button
            onClick={handleReset}
            className="px-4 py-3 rounded-btn text-sm font-bold bg-white border border-pink-border text-ink-secondary hover:text-pink-primary hover:bg-pink-soft transition-all flex items-center gap-1.5"
            title="Reset Simulation"
          >
            <RefreshCw className="w-4 h-4" />
            <span>{t.resetBtn}</span>
          </button>
        </div>
      </div>

      {/* Dynamic Simulation State Explanation Banner */}
      <div className={`p-4 rounded-2xl border-2 transition-all ${
        !isRelayEnabled && stage === 'hop1_reached'
          ? 'bg-status-amber/10 border-status-amber text-status-amber'
          : stage === 'ack_delivered'
          ? 'bg-status-safe/10 border-status-safe text-status-safe'
          : isBroadcasting
          ? 'bg-pink-soft border-pink-primary text-pink-primary'
          : 'bg-bg-app border-pink-border text-ink-secondary'
      }`}>
        <div className="flex items-center gap-3">
          <div className="w-3 h-3 rounded-full flex-shrink-0 animate-ping bg-current" />
          <div className="text-sm font-semibold">
            {!isRelayEnabled && stage === 'hop1_reached' ? (
              <span><strong>Packet Dropped at Phone B:</strong> {t.relayDisabledNotice}</span>
            ) : stage === 'ack_delivered' ? (
              <span><strong>Mesh Full Cycle Complete:</strong> Acknowledgment delivered back to Phone A. Sender knows help is responding!</span>
            ) : stage === 'ack_sending' ? (
              <span><strong>Return ACK Flooding:</strong> Phone C's confirmation packet is hopping backwards across Phone B to Phone A...</span>
            ) : stage === 'hop2_reached' ? (
              <span><strong>Packet Reached Phone C:</strong> Responder's device alarmed with proximity distance bucket and direction!</span>
            ) : stage === 'hop2_sending' ? (
              <span><strong>Hop 2 Re-broadcast:</strong> Phone B forwarded packet with decremented TTL (3 → 2) to Phone C.</span>
            ) : stage === 'hop1_reached' ? (
              <span><strong>Phone B Received Packet:</strong> Processing deduplication filter and scheduling immediate relay...</span>
            ) : stage === 'hop1_sending' ? (
              <span><strong>Hop 1 Broadcasting:</strong> Phone A transmitting 17-byte BLE advertising payload (UUID 0xFDE1)...</span>
            ) : stage === 'countdown' ? (
              <span><strong>SOS Countdown Armed:</strong> 3-second stress buffer. Tap Cancel anytime if triggered accidentally.</span>
            ) : (
              <span><strong>Mesh Ready:</strong> Tap the large pink SOS button on Phone A to begin the offline packet broadcast.</span>
            )}
          </div>
        </div>
      </div>

      {/* 3-PHONE VISUAL CENTERPIECE */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-stretch relative">
        
        {/* ========================================================= */}
        {/* PHONE A: VICTIM (DISTRESS SENDER) */}
        {/* ========================================================= */}
        <div className="flex flex-col bg-bg-app rounded-[32px] p-5 border-2 border-pink-border shadow-flat-card relative">
          
          {/* Top Chassis / Notch */}
          <div className="flex items-center justify-between pb-3 border-b border-pink-border text-xs font-bold text-ink-secondary">
            <div className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-pink-primary" />
              <span className="text-ink-primary font-black">{t.phoneA.title}</span>
            </div>
            <span className="px-2 py-0.5 rounded-full bg-pink-soft text-pink-primary text-[11px] font-bold">
              {t.phoneA.role}
            </span>
          </div>

          {/* Screen Content */}
          <div className="flex-1 flex flex-col justify-between py-6 space-y-5">
            
            {stage === 'idle' && (
              <div className="flex flex-col items-center justify-center py-6 text-center space-y-6">
                {/* 160px Centered Solid Pink SOS Button */}
                <button
                  onClick={handleStartSos}
                  className="w-40 h-40 rounded-full bg-pink-primary active:bg-pink-pressed text-white shadow-pink-action hover:scale-105 active:scale-95 transition-all flex flex-col items-center justify-center border-4 border-white"
                >
                  <Shield className="w-12 h-12 text-white" />
                  <span className="text-sm font-extrabold tracking-wider mt-2">
                    {t.phoneA.holdForHelp}
                  </span>
                </button>

                <div className="space-y-1">
                  <p className="text-sm font-bold text-ink-primary">
                    1-Tap Offline Distress Trigger
                  </p>
                  <p className="text-xs text-ink-secondary">
                    Requires zero internet, SIM card, or pairing.
                  </p>
                </div>
              </div>
            )}

            {stage === 'countdown' && (
              <div className="flex flex-col items-center justify-center py-6 text-center space-y-5">
                <div className="w-28 h-28 rounded-full bg-status-danger text-white flex items-center justify-center text-5xl font-black shadow-lg animate-pulse">
                  {countdown}
                </div>
                <div className="space-y-1">
                  <p className="text-base font-extrabold text-status-danger">
                    {t.phoneA.countdown} {countdown}s
                  </p>
                  <p className="text-xs text-ink-secondary">
                    Capturing offline GPS coordinates...
                  </p>
                </div>
                <button
                  onClick={handleCancel}
                  className="w-full py-3.5 rounded-btn bg-white border-2 border-pink-border text-ink-primary font-bold text-sm hover:bg-pink-soft transition-all"
                >
                  {t.phoneA.cancel}
                </button>
              </div>
            )}

            {isBroadcasting && (
              <div className="space-y-4">
                {/* Broadcasting Status Pill */}
                <div className="flex items-center justify-between px-3.5 py-2 rounded-2xl bg-status-danger/10 border border-status-danger text-status-danger">
                  <div className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-status-danger animate-ping" />
                    <span className="text-xs font-black uppercase tracking-wide">
                      {t.phoneA.broadcasting}
                    </span>
                  </div>
                  <span className="text-[11px] font-mono font-bold">17 Bytes</span>
                </div>

                <div className="bg-white rounded-2xl p-4 border border-pink-border space-y-1">
                  <p className="text-sm font-bold text-ink-primary">
                    Help is being alerted. Stay calm.
                  </p>
                  <p className="text-xs text-ink-secondary">
                    {t.phoneA.meshLine}
                  </p>
                </div>

                {/* Live Responder Counter Card */}
                <div className={`p-4 rounded-2xl border-2 transition-all ${
                  responderCount > 0
                    ? 'bg-status-safe/10 border-status-safe text-status-safe'
                    : 'bg-pink-soft border-pink-border text-ink-primary'
                }`}>
                  <div className="flex items-center gap-3">
                    <div className={`w-10 h-10 rounded-xl flex items-center justify-center text-white ${
                      responderCount > 0 ? 'bg-status-safe' : 'bg-pink-primary'
                    }`}>
                      {responderCount > 0 ? <CheckCircle className="w-5 h-5" /> : <Shield className="w-5 h-5" />}
                    </div>
                    <div>
                      <p className="text-sm font-black">
                        {responderCount > 0 ? t.phoneA.respondersActive : t.phoneA.respondersZero}
                      </p>
                      <p className="text-xs opacity-80">
                        {responderCount > 0 ? t.phoneA.ackNotice : 'Listening for 2-way ACK beacons...'}
                      </p>
                    </div>
                  </div>
                </div>

                {/* "I'm Safe" Button */}
                <button
                  onClick={handleReset}
                  className="w-full py-3.5 rounded-btn bg-status-safe text-white font-extrabold text-sm hover:opacity-90 shadow-sm transition-all"
                >
                  {t.phoneA.imSafe}
                </button>
              </div>
            )}

            {/* Bottom Status Chips */}
            <div className="flex items-center justify-between text-xs font-bold pt-3 border-t border-pink-border text-ink-secondary">
              <span className="flex items-center gap-1 text-status-safe">
                <span className="w-2 h-2 rounded-full bg-status-safe" />
                Bluetooth ON
              </span>
              <span>GPS: 28.6139, 77.2090</span>
            </div>

          </div>

          {/* Animated Packet Arrow to Phone B (Visible on Desktop) */}
          <div className="hidden lg:flex absolute -right-4 top-1/2 -translate-y-1/2 z-20">
            <div className={`w-8 h-8 rounded-full border-2 flex items-center justify-center shadow-md transition-all ${
              stage === 'hop1_sending'
                ? 'bg-pink-primary text-white border-white animate-bounce'
                : isPacketAtB
                ? 'bg-status-safe text-white border-white'
                : 'bg-white text-ink-secondary border-pink-border'
            }`}>
              <ArrowRight className="w-4 h-4" />
            </div>
          </div>
        </div>

        {/* ========================================================= */}
        {/* PHONE B: RELAY NODE (MESH FORWARDER) */}
        {/* ========================================================= */}
        <div className={`flex flex-col rounded-[32px] p-5 border-2 shadow-flat-card relative transition-all ${
          isRelayEnabled ? 'bg-bg-app border-pink-border' : 'bg-gray-100 border-gray-300 opacity-80'
        }`}>
          
          {/* Top Chassis / Notch */}
          <div className="flex items-center justify-between pb-3 border-b border-pink-border text-xs font-bold text-ink-secondary">
            <div className="flex items-center gap-1.5">
              <span className={`w-2.5 h-2.5 rounded-full ${isRelayEnabled ? 'bg-status-guardian' : 'bg-gray-400'}`} />
              <span className="text-ink-primary font-black">{t.phoneB.title}</span>
            </div>
            <span className={`px-2 py-0.5 rounded-full text-[11px] font-bold ${
              isRelayEnabled ? 'bg-blue-100 text-status-guardian' : 'bg-gray-200 text-gray-700'
            }`}>
              {isRelayEnabled ? t.phoneB.statusActive : t.phoneB.statusDisabled}
            </span>
          </div>

          {/* Screen Content */}
          <div className="flex-1 flex flex-col justify-between py-6 space-y-5">
            
            {!isRelayEnabled ? (
              <div className="flex flex-col items-center justify-center py-10 text-center space-y-3">
                <div className="w-16 h-16 rounded-full bg-gray-200 text-gray-500 flex items-center justify-center">
                  <WifiOff className="w-8 h-8" />
                </div>
                <h4 className="text-base font-extrabold text-gray-800">
                  Relay Phone Disabled
                </h4>
                <p className="text-xs text-gray-600 max-w-xs">
                  This phone is powered off or relay is disabled. Incoming packets cannot be forwarded to distant responders.
                </p>
                {stage === 'hop1_reached' && (
                  <div className="px-3 py-1.5 rounded-xl bg-status-danger/10 text-status-danger border border-status-danger text-xs font-bold">
                    Packet Dropped: Cannot Bridge to Phone C
                  </div>
                )}
              </div>
            ) : !isPacketAtB ? (
              <div className="flex flex-col items-center justify-center py-10 text-center space-y-3">
                <div className="w-16 h-16 rounded-full bg-blue-50 text-status-guardian flex items-center justify-center border border-blue-200">
                  <Radio className="w-8 h-8 animate-pulse" />
                </div>
                <h4 className="text-base font-extrabold text-ink-primary">
                  Relay Standing By
                </h4>
                <p className="text-xs text-ink-secondary max-w-xs">
                  {t.phoneB.waiting}
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {/* Hop Telemetry Card */}
                <div className="bg-white rounded-2xl p-4 border border-pink-border space-y-3">
                  <div className="flex items-center justify-between text-xs font-bold text-ink-secondary">
                    <span>BLE Flooding Forwarder</span>
                    <span className="text-status-guardian font-mono">UUID 0xFDE1</span>
                  </div>

                  <div className="space-y-1">
                    <p className="text-sm font-extrabold text-ink-primary">
                      {stage === 'hop1_reached' ? 'Packet Received from Sender #7842' : 'Packet Re-broadcasted as Hop 2'}
                    </p>
                    <p className="text-xs text-ink-secondary font-mono">
                      TTL: 3 → 2 | Hop Count: 1 → 2
                    </p>
                  </div>

                  {/* 2-Column Telemetry Counter Tiles */}
                  <div className="grid grid-cols-2 gap-2 pt-1 font-mono text-center">
                    <div className="bg-bg-app p-2.5 rounded-xl border border-pink-border">
                      <p className="text-xs text-ink-secondary">Packets In</p>
                      <p className="text-lg font-black text-status-guardian">1</p>
                    </div>
                    <div className="bg-bg-app p-2.5 rounded-xl border border-pink-border">
                      <p className="text-xs text-ink-secondary">Relayed Out</p>
                      <p className="text-lg font-black text-status-safe">1</p>
                    </div>
                  </div>
                </div>

                {/* Packet Deduplication Status */}
                <div className="p-3 rounded-2xl bg-blue-50 border border-blue-200 text-status-guardian text-xs font-semibold flex items-center gap-2">
                  <Activity className="w-4 h-4 flex-shrink-0" />
                  <span>Deduplication cache active: Identical sender hash dropped to prevent storming.</span>
                </div>
              </div>
            )}

            {/* Bottom Status */}
            <div className="flex items-center justify-between text-xs font-bold pt-3 border-t border-pink-border text-ink-secondary">
              <span className="flex items-center gap-1">
                <span className={`w-2 h-2 rounded-full ${isRelayEnabled ? 'bg-status-safe' : 'bg-gray-400'}`} />
                {isRelayEnabled ? 'Hop Budget: 3' : 'Offline'}
              </span>
              <span>Mesh Flooding: Active</span>
            </div>

          </div>

          {/* Animated Packet Arrow to Phone C (Visible on Desktop) */}
          <div className="hidden lg:flex absolute -right-4 top-1/2 -translate-y-1/2 z-20">
            <div className={`w-8 h-8 rounded-full border-2 flex items-center justify-center shadow-md transition-all ${
              !isRelayEnabled
                ? 'bg-status-danger text-white border-white'
                : stage === 'hop2_sending'
                ? 'bg-pink-primary text-white border-white animate-bounce'
                : isPacketAtC
                ? 'bg-status-safe text-white border-white'
                : 'bg-white text-ink-secondary border-pink-border'
            }`}>
              {!isRelayEnabled ? <XCircle className="w-4 h-4" /> : <ArrowRight className="w-4 h-4" />}
            </div>
          </div>
        </div>

        {/* ========================================================= */}
        {/* PHONE C: GUARDIAN / RESPONDER */}
        {/* ========================================================= */}
        <div className="flex flex-col bg-bg-app rounded-[32px] p-5 border-2 border-pink-border shadow-flat-card relative">
          
          {/* Top Chassis / Notch */}
          <div className="flex items-center justify-between pb-3 border-b border-pink-border text-xs font-bold text-ink-secondary">
            <div className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-status-safe" />
              <span className="text-ink-primary font-black">{t.phoneC.title}</span>
            </div>
            <span className="px-2 py-0.5 rounded-full bg-emerald-100 text-status-safe text-[11px] font-bold">
              {t.phoneC.role}
            </span>
          </div>

          {/* Screen Content */}
          <div className="flex-1 flex flex-col justify-between py-6 space-y-5">
            
            {!isPacketAtC ? (
              <div className="flex flex-col items-center justify-center py-10 text-center space-y-3">
                <div className="w-16 h-16 rounded-full bg-emerald-50 text-status-safe flex items-center justify-center border border-emerald-200">
                  <Radio className="w-8 h-8 animate-pulse" />
                </div>
                <h4 className="text-base font-extrabold text-ink-primary">
                  {t.phoneC.status}
                </h4>
                <p className="text-xs text-ink-secondary max-w-xs">
                  {t.phoneC.waiting}
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {/* Emergency Alert Banner */}
                <div className="bg-status-danger rounded-2xl p-4 text-white shadow-sm space-y-1">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <AlertCircle className="w-5 h-5 animate-bounce" />
                      <span className="text-xs font-black uppercase tracking-wider">
                        {t.phoneC.alertTitle}
                      </span>
                    </div>
                    <span className="text-[10px] font-mono bg-white/20 px-2 py-0.5 rounded">
                      Sender #7842
                    </span>
                  </div>
                  <p className="text-xs font-semibold opacity-90">
                    BLE Beacon detected via relay hop.
                  </p>
                </div>

                {/* Distance Bucket & Direction Arrow */}
                <div className="bg-white rounded-2xl p-4 border border-pink-border space-y-2">
                  <div className="flex items-center justify-between">
                    <div className="space-y-0.5">
                      <p className="text-xs text-ink-secondary font-bold">Estimated Proximity</p>
                      <p className="text-2xl font-black text-status-safe">
                        {t.phoneC.distanceBucket}
                      </p>
                    </div>
                    <div className="w-10 h-10 rounded-full bg-pink-soft text-pink-primary flex items-center justify-center">
                      <Navigation className="w-5 h-5 rotate-45" />
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-xs font-mono text-ink-secondary pt-1 border-t border-pink-border">
                    <span className="px-2 py-0.5 rounded bg-blue-50 text-status-guardian font-bold">
                      {t.phoneC.hopsBadge}
                    </span>
                    <span>RSSI: -58 dBm</span>
                  </div>
                </div>

                {/* 1-Tap Responder ACK Action */}
                <button
                  onClick={handleSendAck}
                  disabled={isAckSent}
                  className={`w-full py-4 rounded-btn text-sm font-extrabold shadow-sm transition-all flex items-center justify-center gap-2 ${
                    isAckSent
                      ? 'bg-status-safe text-white cursor-default'
                      : 'bg-status-safe active:bg-emerald-800 text-white hover:opacity-95'
                  }`}
                >
                  <CheckCircle className="w-5 h-5" />
                  <span>{isAckSent ? t.phoneC.ackSentBtn : t.phoneC.respondingBtn}</span>
                </button>

                {/* Deterrent Tools */}
                <div className="grid grid-cols-2 gap-2">
                  <button
                    onClick={() => setIsSirenOn(!isSirenOn)}
                    className={`py-2.5 rounded-btn text-xs font-bold border transition-all flex items-center justify-center gap-1.5 ${
                      isSirenOn ? 'bg-status-danger text-white border-status-danger' : 'bg-white text-ink-primary border-pink-border hover:bg-pink-soft'
                    }`}
                  >
                    <Volume2 className="w-4 h-4" />
                    <span>{isSirenOn ? 'Siren Playing' : t.phoneC.sirenBtn}</span>
                  </button>
                  <button
                    onClick={() => setIsTorchOn(!isTorchOn)}
                    className={`py-2.5 rounded-btn text-xs font-bold border transition-all flex items-center justify-center gap-1.5 ${
                      isTorchOn ? 'bg-status-amber text-white border-status-amber' : 'bg-white text-ink-primary border-pink-border hover:bg-pink-soft'
                    }`}
                  >
                    <Flashlight className="w-4 h-4" />
                    <span>{isTorchOn ? 'Torch Active' : t.phoneC.torchBtn}</span>
                  </button>
                </div>
              </div>
            )}

            {/* Bottom Status */}
            <div className="flex items-center justify-between text-xs font-bold pt-3 border-t border-pink-border text-ink-secondary">
              <span className="flex items-center gap-1 text-status-safe">
                <span className="w-2 h-2 rounded-full bg-status-safe" />
                Guardian Active
              </span>
              <span>Offline GPS Cached</span>
            </div>

          </div>

        </div>

      </div>

      {/* Caption Footer: Explaining the Flooding Mesh Algorithm */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t border-pink-border text-xs text-ink-secondary">
        <div className="bg-bg-app p-3 rounded-2xl border border-pink-border">
          <p className="font-bold text-ink-primary">1. Zero Infrastructure</p>
          <p className="mt-0.5">Alert travels over raw Bluetooth Low Energy advertisements (0xFDE1). Works without internet or mobile towers.</p>
        </div>
        <div className="bg-bg-app p-3 rounded-2xl border border-pink-border">
          <p className="font-bold text-ink-primary">2. Flooding & Deduplication</p>
          <p className="mt-0.5">Every bystander phone acts as an autonomous relay node. LRU cache prevents broadcast loops and battery drain.</p>
        </div>
        <div className="bg-bg-app p-3 rounded-2xl border border-pink-border">
          <p className="font-bold text-ink-primary">3. Two-Way Confirmation</p>
          <p className="mt-0.5">When a responder acknowledges the alert, an ACK beacon bounces back through the mesh to reassure the victim.</p>
        </div>
      </div>

    </section>
  )
}
