import React, { useState, useEffect } from 'react'
import { AlertCircle, Radio, Shield, CheckCircle, Volume2, Flashlight, Calculator, RefreshCw, Smartphone, MapPin, Signal, ArrowRight, XCircle } from 'lucide-react'

export default function InteractiveSimulator() {
  const [activeMode, setActiveMode] = useState('sos') // 'sos' | 'decoy'
  
  // Phone A (Distress Sender) State
  const [sosState, setSosState] = useState('idle') // 'idle' | 'countdown' | 'broadcasting'
  const [countdown, setCountdown] = useState(3)
  const [responderCount, setResponderCount] = useState(0)

  // Phone B (Intermediate Mesh Relay) State
  const [phoneBAlert, setPhoneBAlert] = useState(null)
  const [isRelayEnabled, setIsRelayEnabled] = useState(true)
  const [isPhoneBRelaying, setIsPhoneBRelaying] = useState(false)

  // Phone C (Distant Responder - Out of Direct Range of A) State
  const [phoneCAlert, setPhoneCAlert] = useState(null)
  const [isPhoneCAckSent, setIsPhoneCAckSent] = useState(false)
  const [isPhoneCSirenOn, setIsPhoneCSirenOn] = useState(false)
  const [isPhoneCTorchOn, setIsPhoneCTorchOn] = useState(false)

  // Decoy Calculator State
  const [calcDisplay, setCalcDisplay] = useState('0')
  const [calcInputHistory, setCalcInputHistory] = useState('')
  const [secretPin, setSecretPin] = useState('1234')

  // Countdown timer logic for Phone A
  useEffect(() => {
    let timer
    if (sosState === 'countdown') {
      if (countdown > 0) {
        timer = setTimeout(() => setCountdown((prev) => prev - 1), 1000)
      } else {
        setSosState('broadcasting')
        
        // Hop 1: Phone A -> Phone B (Direct BLE radio in range, TTL 4)
        setTimeout(() => {
          setPhoneBAlert({
            senderId: '#7842',
            timestamp: new Date().toLocaleTimeString(),
            lat: 28.6139,
            lon: 77.2090,
            rssi: -58,
            ttl: 4,
            distanceText: 'Approx. < 3 meters away',
            category: 'VERY CLOSE'
          })

          // Hop 2: If Relay is enabled on B, B forwards to C with TTL 3
          if (isRelayEnabled) {
            setIsPhoneBRelaying(true)
            setTimeout(() => {
              setPhoneCAlert({
                senderId: '#7842',
                timestamp: new Date().toLocaleTimeString(),
                lat: 28.6139,
                lon: 77.2090,
                rssi: -79,
                ttl: 3, // Hop count: 1 (decremented TTL)
                relayedBy: '#B-RELAY',
                distanceText: 'Relayed via Phone B (Multi-Hop)',
                category: 'RELAYED'
              })
              setIsPhoneBRelaying(false)
            }, 1200)
          }
        }, 1000)
      }
    }
    return () => clearTimeout(timer)
  }, [sosState, countdown, isRelayEnabled])

  const handleTriggerSos = () => {
    setSosState('countdown')
    setCountdown(3)
    setResponderCount(0)
    setPhoneBAlert(null)
    setPhoneCAlert(null)
    setIsPhoneCAckSent(false)
  }

  const handleCancelCountdown = () => {
    setSosState('idle')
    setCountdown(3)
  }

  const handleResetSimulator = () => {
    setSosState('idle')
    setCountdown(3)
    setResponderCount(0)
    setPhoneBAlert(null)
    setPhoneCAlert(null)
    setIsPhoneCAckSent(false)
    setIsPhoneBRelaying(false)
    setIsPhoneCSirenOn(false)
    setIsPhoneCTorchOn(false)
    setCalcDisplay('0')
    setCalcInputHistory('')
  }

  const handlePhoneCSendAck = () => {
    setIsPhoneCAckSent(true)
    if (isRelayEnabled) {
      // ACK travels C -> B -> A
      setTimeout(() => {
        setResponderCount((prev) => prev + 1)
      }, 800)
    }
  }

  // Calculator interaction
  const handleCalcBtn = (val) => {
    if (val === 'C') {
      setCalcDisplay('0')
      setCalcInputHistory('')
      return
    }
    if (val === '=') {
      if (calcDisplay === secretPin || calcInputHistory.endsWith(secretPin)) {
        setActiveMode('sos')
        handleTriggerSos()
        return
      }
      try {
        const clean = calcDisplay.replace(/×/g, '*').replace(/÷/g, '/')
        if (/^[0-9+\-*/. %()]+$/.test(clean)) {
          const res = new Function(`return (${clean})`)()
          setCalcDisplay(String(res))
        } else {
          setCalcDisplay('Error')
        }
      } catch (e) {
        setCalcDisplay('Error')
      }
      return
    }
    setCalcInputHistory((prev) => prev + val)
    if (calcDisplay === '0' || calcDisplay === 'Error') {
      setCalcDisplay(val)
    } else {
      setCalcDisplay((prev) => prev + val)
    }
  }

  return (
    <section className="bg-white rounded-3xl p-6 lg:p-8 border border-pink-200 shadow-soft-pink space-y-6">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-pink-100">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-[#FF2D78] animate-pulse" />
            <h3 className="text-2xl font-black text-[#2D1522]">
              Interactive 3-Phone BLE Mesh Simulation
            </h3>
            <span className="text-[10px] uppercase font-bold tracking-wider px-2.5 py-0.5 rounded-full bg-emerald-100 text-emerald-800 border border-emerald-300">
              Multi-Hop Flooding Mesh Verified
            </span>
          </div>
          <p className="text-xs sm:text-sm text-gray-600 font-medium mt-1">
            Simulate 3-node multi-hop propagation: Phone A (Victim) reaches Phone C (Responder) via Phone B (Relay) even when A and C are out of direct radio range.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <div className="flex bg-pink-50 p-1 rounded-xl border border-pink-200">
            <button
              onClick={() => setActiveMode('sos')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                activeMode === 'sos' ? 'bg-[#FF2D78] text-white shadow-sm' : 'text-gray-600 hover:text-pink-600'
              }`}
            >
              Standard SOS Mode
            </button>
            <button
              onClick={() => setActiveMode('decoy')}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                activeMode === 'decoy' ? 'bg-[#FF2D78] text-white shadow-sm' : 'text-gray-600 hover:text-pink-600'
              }`}
            >
              Decoy Calculator Mode
            </button>
          </div>

          <button
            onClick={handleResetSimulator}
            className="p-2 rounded-xl text-gray-500 hover:text-[#FF2D78] hover:bg-pink-50 border border-pink-200 transition-colors"
            title="Reset Simulator"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Mesh Path Flow Indicator */}
      <div className="bg-gradient-to-r from-pink-50 via-purple-50 to-emerald-50 rounded-2xl p-3 border border-pink-200 flex flex-wrap items-center justify-between gap-2 text-xs font-bold">
        <div className="flex items-center gap-2">
          <span className="px-2 py-0.5 bg-pink-600 text-white rounded-md text-[11px]">Phone A (Victim)</span>
          <ArrowRight className="w-3.5 h-3.5 text-gray-400" />
          <span className="px-2 py-0.5 bg-purple-600 text-white rounded-md text-[11px]">Phone B (Relay)</span>
          <ArrowRight className="w-3.5 h-3.5 text-gray-400" />
          <span className="px-2 py-0.5 bg-emerald-600 text-white rounded-md text-[11px]">Phone C (Distant Responder)</span>
        </div>
        <div className="flex items-center gap-2 text-[11px]">
          <span className="text-gray-500">Topology:</span>
          <span className="text-purple-700 font-extrabold">A & C out of range • B bridges the gap</span>
        </div>
      </div>

      {/* 3-Device Multi-Hop Interactive Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5 items-start">
        
        {/* ================= DEVICE 1 (PHONE A: SENDER) ================= */}
        <div className="bg-gradient-to-b from-[#FFF5F8] to-white rounded-3xl p-5 border-2 border-pink-200 shadow-md space-y-3.5">
          <div className="flex items-center justify-between pb-2.5 border-b border-pink-100">
            <div className="flex items-center gap-1.5">
              <Smartphone className="w-4 h-4 text-[#FF2D78]" />
              <h4 className="text-xs font-extrabold text-[#2D1522]">Phone A (Victim)</h4>
            </div>
            <span className="text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-pink-100 text-[#FF2D78]">
              TTL: 4 (Origin)
            </span>
          </div>

          {activeMode === 'decoy' ? (
            /* Decoy Calculator Interface */
            <div className="bg-[#1E1E1E] rounded-2xl p-3.5 text-white shadow-inner max-w-[240px] mx-auto space-y-2">
              <div className="text-right">
                <p className="text-[9px] text-gray-400">Calculator Disguise</p>
                <div className="text-2xl font-light font-mono py-1">{calcDisplay}</div>
              </div>
              <div className="grid grid-cols-4 gap-1.5 text-xs font-semibold">
                {['C', '%', '÷', '×', '7', '8', '9', '-', '4', '5', '6', '+', '1', '2', '3', '='].map((btn) => (
                  <button
                    key={btn}
                    onClick={() => handleCalcBtn(btn)}
                    className="h-8 rounded-lg bg-[#333333] hover:bg-[#444444] flex items-center justify-center"
                  >
                    {btn}
                  </button>
                ))}
              </div>
              <p className="text-[10px] text-pink-300 text-center">PIN 1234 then = to trigger SOS</p>
            </div>
          ) : (
            /* SOS Trigger Interface */
            <div className="flex flex-col items-center justify-center py-4 text-center space-y-3">
              {sosState === 'idle' && (
                <>
                  <div className="relative">
                    <div className="w-28 h-28 rounded-full bg-pink-100 flex items-center justify-center animate-pulse-ring" />
                    <button
                      onClick={handleTriggerSos}
                      className="absolute inset-0 m-auto w-24 h-24 rounded-full bg-gradient-to-tr from-[#FF2D78] to-[#E91E63] text-white shadow-xl flex flex-col items-center justify-center hover:scale-105 active:scale-95 transition-all"
                    >
                      <AlertCircle className="w-8 h-8" />
                      <span className="text-[10px] font-black tracking-wider mt-0.5">TAP SOS</span>
                    </button>
                  </div>
                  <p className="text-[11px] text-gray-500">Tap or simulate 5 power toggles</p>
                </>
              )}

              {sosState === 'countdown' && (
                <div className="space-y-2 w-full">
                  <div className="w-16 h-16 rounded-full bg-pink-600 text-white flex items-center justify-center text-2xl font-black mx-auto animate-bounce">
                    {countdown}s
                  </div>
                  <p className="text-xs font-extrabold text-[#E91E63]">SOS Countdown ({countdown}s)</p>
                  <button
                    onClick={handleCancelCountdown}
                    className="w-full py-2 rounded-xl bg-gray-900 text-white text-xs font-bold"
                  >
                    I'M SAFE (CANCEL)
                  </button>
                </div>
              )}

              {sosState === 'broadcasting' && (
                <div className="space-y-3 w-full">
                  <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-red-100 text-red-700 text-[10px] font-bold animate-pulse">
                    <Radio className="w-3 h-3" />
                    <span>Broadcasting BLE (0xFDE1, TTL 4)</span>
                  </div>

                  <div className={`p-3 rounded-xl border text-left ${
                    responderCount > 0 ? 'bg-emerald-50 border-emerald-300 text-emerald-900' : 'bg-pink-50 border-pink-200 text-pink-900'
                  }`}>
                    <p className="text-xs font-black">
                      {responderCount > 0 ? `👥 ${responderCount} Remote Responder(s) Confirmed!` : 'Awaiting Peer ACKs...'}
                    </p>
                    <p className="text-[10px] opacity-80 mt-0.5">
                      {responderCount > 0 ? 'Confirmation received via Mesh Relay (Phone B)!' : 'Direct reach: Phone B only'}
                    </p>
                  </div>

                  <button
                    onClick={handleResetSimulator}
                    className="w-full py-2 rounded-xl bg-emerald-600 text-white text-xs font-bold"
                  >
                    STOP BROADCAST
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        {/* ================= DEVICE 2 (PHONE B: MESH RELAY) ================= */}
        <div className="bg-gradient-to-b from-[#FAF5FF] to-white rounded-3xl p-5 border-2 border-purple-200 shadow-md space-y-3.5">
          <div className="flex items-center justify-between pb-2.5 border-b border-purple-100">
            <div className="flex items-center gap-1.5">
              <Smartphone className="w-4 h-4 text-purple-600" />
              <h4 className="text-xs font-extrabold text-[#2D1522]">Phone B (Relay Node)</h4>
            </div>
            <span className="text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-purple-100 text-purple-700">
              Mesh Forwarder
            </span>
          </div>

          {/* Relay Control Toggle */}
          <div className="bg-white rounded-xl p-3 border border-purple-200 flex items-center justify-between">
            <div>
              <p className="text-xs font-bold text-gray-800">Relay Forwarding</p>
              <p className="text-[10px] text-gray-500">{isRelayEnabled ? 'Active (Forwards TTL-1)' : 'Disabled (Breaks Mesh)'}</p>
            </div>
            <button
              onClick={() => setIsRelayEnabled(!isRelayEnabled)}
              className={`px-2.5 py-1 rounded-lg text-[10px] font-bold transition-all ${
                isRelayEnabled
                  ? 'bg-purple-600 hover:bg-purple-700 text-white shadow-sm'
                  : 'bg-gray-200 hover:bg-gray-300 text-gray-700'
              }`}
            >
              {isRelayEnabled ? 'Disable Relay' : 'Enable Relay'}
            </button>
          </div>

          {phoneBAlert ? (
            <div className="space-y-2.5 text-left text-xs animate-fadeIn">
              <div className="bg-purple-600 text-white p-3 rounded-xl">
                <p className="font-black text-xs">🚨 DISTRESS RECEIVED</p>
                <p className="text-[10px] opacity-90 mt-0.5">Sender: {phoneBAlert.senderId} • Ingress TTL: {phoneBAlert.ttl}</p>
              </div>

              <div className="bg-white p-3 rounded-xl border border-purple-200 space-y-1">
                <div className="flex justify-between text-[11px] font-bold text-gray-600">
                  <span>Relay Policy Decision:</span>
                  <span className={isRelayEnabled ? 'text-purple-700 font-mono' : 'text-red-600'}>
                    {isRelayEnabled ? 'ALERT_AND_RELAY (TTL: 3)' : 'ALERT_ONLY (Relay Off)'}
                  </span>
                </div>
                <p className="text-[10px] text-gray-500">
                  {isRelayEnabled
                    ? (isPhoneBRelaying ? '⚡ Transmitting 10s relay beacon...' : '✓ Relay frame forwarded to Phone C')
                    : 'Relay suppressed by manual toggle'}
                </p>
              </div>
            </div>
          ) : (
            <div className="py-12 text-center text-xs text-gray-400 space-y-2">
              <Radio className="w-6 h-6 mx-auto text-purple-400 animate-pulse" />
              <p>Relay Scanner Idle</p>
              <p className="text-[10px] text-gray-400">Listens for Phone A and bridges to Phone C</p>
            </div>
          )}
        </div>

        {/* ================= DEVICE 3 (PHONE C: DISTANT RESPONDER) ================= */}
        <div className="bg-gradient-to-b from-[#F0FDF4] to-white rounded-3xl p-5 border-2 border-emerald-200 shadow-md space-y-3.5">
          <div className="flex items-center justify-between pb-2.5 border-b border-emerald-100">
            <div className="flex items-center gap-1.5">
              <Smartphone className="w-4 h-4 text-emerald-600" />
              <h4 className="text-xs font-extrabold text-[#2D1522]">Phone C (Distant Peer)</h4>
            </div>
            <span className="text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-emerald-100 text-emerald-700">
              Out of A's Range
            </span>
          </div>

          {phoneCAlert ? (
            <div className="space-y-2.5 text-left text-xs animate-fadeIn">
              <div className="bg-red-600 text-white p-3 rounded-xl shadow-md">
                <div className="flex items-center justify-between">
                  <span className="font-black text-xs">🚨 RELAYED SOS ALARM!</span>
                  <span className="text-[10px] font-mono bg-white/20 px-1.5 py-0.5 rounded">TTL: {phoneCAlert.ttl}</span>
                </div>
                <p className="text-[10px] opacity-90 mt-0.5">Victim ID: {phoneCAlert.senderId} (via {phoneCAlert.relayedBy})</p>
              </div>

              <div className="bg-white p-2.5 rounded-xl border border-emerald-200 space-y-1">
                <p className="text-[10px] text-gray-500 font-bold">Offline Coordinates:</p>
                <p className="font-mono font-bold text-gray-800 text-[11px]">{phoneCAlert.lat}, {phoneCAlert.lon}</p>
              </div>

              <button
                onClick={handlePhoneCSendAck}
                disabled={isPhoneCAckSent}
                className={`w-full py-2.5 rounded-xl text-xs font-black tracking-wide transition-all ${
                  isPhoneCAckSent
                    ? 'bg-emerald-700 text-white cursor-default'
                    : 'bg-emerald-600 hover:bg-emerald-700 text-white shadow-md active:scale-95'
                }`}
              >
                {isPhoneCAckSent ? '✓ ACK TRANSMITTED VIA RELAY' : 'SEND RESPONDER ACK'}
              </button>

              <div className="grid grid-cols-2 gap-1.5">
                <button
                  onClick={() => setIsPhoneCSirenOn(!isPhoneCSirenOn)}
                  className={`py-1.5 rounded-lg text-[10px] font-bold flex items-center justify-center gap-1 border ${
                    isPhoneCSirenOn ? 'bg-red-600 text-white' : 'bg-white text-gray-700'
                  }`}
                >
                  <Volume2 className="w-3 h-3" />
                  <span>{isPhoneCSirenOn ? 'Siren On' : 'Siren'}</span>
                </button>
                <button
                  onClick={() => setIsPhoneCTorchOn(!isPhoneCTorchOn)}
                  className={`py-1.5 rounded-lg text-[10px] font-bold flex items-center justify-center gap-1 border ${
                    isPhoneCTorchOn ? 'bg-amber-500 text-white' : 'bg-white text-gray-700'
                  }`}
                >
                  <Flashlight className="w-3 h-3" />
                  <span>{isPhoneCTorchOn ? 'Torch On' : 'Torch'}</span>
                </button>
              </div>
            </div>
          ) : (
            <div className="py-12 text-center text-xs text-gray-400 space-y-2">
              <Radio className="w-6 h-6 mx-auto text-emerald-400" />
              <p>Out of Direct Range of Phone A</p>
              <p className="text-[10px] text-gray-400">
                {isRelayEnabled ? 'Will ring only when Phone B relays the packet' : 'Relay disabled on B: will receive nothing'}
              </p>
            </div>
          )}
        </div>

      </div>

    </section>
  )
}
