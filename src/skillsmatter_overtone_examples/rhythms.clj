(ns skillsmatter-overtone-examples.rhythms
  (:use overtone.live))

;; First, we'll define some percussive instruments

;; this high hat instrument takes a white noise generator and shapes
;; it with a percussive envelope

(definst hat [volume 1.0]
  (let [src (white-noise)
        env (env-gen (perc 0.001 0.3) :action FREE)]
    (* volume 3 src env)))

(comment
  (hat)
  )

;; pretty crude kick, made from 2 sin oscillators with rapidly
;; decreasing frequencies, shaped by a percussive envelope

(definst kick [volume 1.0]
  (let [body-freq (* 220 (env-gen (lin-env 0.01 0 0.3 1) :action NO-ACTION))
        body (sin-osc body-freq)
        
        pop-freq (+ 220 (* 200 (env-gen (lin-env 0.01 0 0.1 1) :action NO-ACTION)))
        pop  (sin-osc pop-freq)
        
        env  (env-gen (perc 0.001 0.25) :action FREE)
        ]
    (* 4 env (+ body pop))))

(comment
  (kick)
  )

;; we can schedule beats for the future with the at macro:

(comment
  (at (+ 1000 (now)) (kick))
  )

;; ...and chain multiple beats together with a do form:

(comment
  (let
      [time (now)]
    (at (+    0 time) (kick) )
    (at (+  400 time) (hat)  )
    (at (+  800 time) (kick) )
    (at (+ 1200 time) (hat)  ))
  )

;; to repeat, we use the apply-at macro to schedule a recursive call
;; for the future

(defn loop-beats [time]
  (at (+    0 time) (kick) )
  (at (+  400 time) (hat)  )
  (at (+  800 time) (kick) )
  (at (+ 1200 time) (hat)  )
  (apply-at (+ 1600 time) loop-beats (+ 1600 time) []))

(comment
  (loop-beats (now))
  )

;; rather than thinking in terms of milliseconds, it's useful to think
;; in terms of beats. We can create a metronome to help with this. A
;; metronome counts beats over time. Here's a metronome at 180 beats
;; per minute (bpm):

(def metro (metronome 180))

;; we use it as follows:

(metro) ; current beat number
(metro 3) ; timestamp of beat number 3

;; if we rewrite loop-beats using a metronome, it would look like
;; this:

(defn metro-beats [m beat-num]
  (at (m (+ 0 beat-num)) (kick))
  (at (m (+ 1 beat-num)) (hat))
  (at (m (+ 2 beat-num)) (kick))
  (at (m (+ 3 beat-num)) (hat))
  (apply-at (m (+ 4 beat-num)) metro-beats m (+ 4 beat-num) [])
  )

(comment
  (metro-beats metro (metro))
  )

;; because we're using a metronome, we can change the speed:

(comment
  (metro :bpm 120) ;slower
  (metro :bpm 240) ;faster
  )

;; a more complex rhythm

(defn weak-hat []
  (hat 0.3))

(defn phat-beats [m beat-num]
  (at (m (+ 0 beat-num)) (kick) (weak-hat))

  (at (m (+ 2 beat-num)) (hat))
  (at (m (+ 3 beat-num)) (kick))
  (at (m (+ 4 beat-num)) (kick) (weak-hat))

  (at (m (+ 6 beat-num)) (kick) (hat) )

  (apply-at (m (+ 8 beat-num)) phat-beats m (+ 8 beat-num) [])
  )

(comment
  (phat-beats metro (metro))
  )