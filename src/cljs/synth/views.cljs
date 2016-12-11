(ns synth.views
  (:require [synth.synth :as s]
            [synth.components :as c]
            [reagent.core :as r]))

(defonce app-state
  (r/atom
    {:wave :sawtooth}))

(def waves [:sawtooth :square :triangle :sine])

(defn main-page
  []
  [:div {:class "app"}
    [:h1 "electron polysynth"]
    [:div
      [c/knob "vibe speed"  :vibrato-speed]
      [c/knob "vibe depth"  :vibrato-depth]
      [c/knob "filter q"    :filter-q]
      [c/knob "filter-freq" :filter-freq]]

    [:div {:class "button-container"}
      (for [w waves]
          ^{:key w}[c/wave-button w])
      [:div "osc waveforms"]]

    [:div
      [c/knob "attack"  :attack-time]
      [c/knob "decay"   :decay-time]
      [c/knob "sustain" :sustain-level]
      [c/knob "release" :release-time]]])

