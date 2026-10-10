import React from 'react'
import { Shield, Heart } from 'lucide-react'

export default function BottomBar({ onTriggerQuickDemo }) {
  return (
    <div className="fixed bottom-0 inset-x-0 z-30 bg-white border-t border-pink-border px-4 py-3 shadow-flat-elevated">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-4">
        
        {/* Left message */}
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-pink-soft flex items-center justify-center text-pink-primary">
            <Shield className="w-4 h-4 stroke-[2.5]" />
          </div>
          <div>
            <p className="text-xs sm:text-sm font-extrabold text-ink-primary">
              You are not alone. Help is just a tap away.
            </p>
            <p className="hidden sm:block text-[11px] text-ink-secondary font-medium">
              Offline BLE broadcast is active & listening peer-to-peer.
            </p>
          </div>
        </div>

        {/* Right Action Button */}
        <div className="flex items-center gap-2">
          <button
            onClick={onTriggerQuickDemo}
            className="flex items-center gap-2 px-5 py-2.5 rounded-btn text-xs font-black text-white bg-pink-primary hover:bg-pink-pressed active:scale-95 shadow-pink-action transition-all"
          >
            <Heart className="w-3.5 h-3.5 fill-white" />
            <span>Launch Mesh Demo</span>
          </button>
        </div>

      </div>
    </div>
  )
}
