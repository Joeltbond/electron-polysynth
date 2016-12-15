(ns synth.audio.utils)

(defn connect [& nodes]
  (doall
    (map (fn [[a b]] (.connect a b))
         (partition 2 1 nodes))))