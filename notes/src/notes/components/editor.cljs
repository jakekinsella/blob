(ns notes.components.editor
  (:require
    [clojure.math :as math]
    [reagent.core :as r]
    [re-frame.core :as re-frame]
    [react :as react]
    [spade.core :refer [defclass]]
    [central :as central]
    [notes.events :as events]
    [notes.subs :as subs]))

(defclass textbox-style []
  {:width "98%"
   :height "93%"
   :padding-left "1%"
   :padding-right "1%"
   :padding-top "58px"
   :background "transparent"
   :color central/Constants.Colors.Text.base
   :border "none"
   :font-size "15px"
   :font-family "'Roboto', sans-serif"
   :font-weight "100"
   :user-select "none"
   :resize "none"}
  [:&:focus {:outline "none"}]
  (at-media {:max-width "750px"}
    {:font-size "17px"}))
(defn textbox [attrs]
  [:textarea (merge-with + {:class (textbox-style) :required true} attrs)])

(defonce title (r/atom nil))
(defonce body (r/atom nil))

(defn text-editor []
  [textbox {:value @body
            :on-change (fn [event]
                         (let [value (-> event .-target .-value)]
                           (reset! body value)
                           (re-frame/dispatch [::events/save-note @title value])))}])

(defonce width (r/atom nil))
(defonce height (r/atom nil))
(defonce pressed (r/atom false))
(defonce points (r/atom []))

(defclass canvas-style []
  {:background (str "repeating-linear-gradient(" central/Constants.Colors.Container.background ", " central/Constants.Colors.Container.background " 60px, #777 61px)")})
(defn canvas-editor []
  (let [ref (react/useRef)
        drawing @(re-frame/subscribe [::subs/drawing])]
    (do (react/useEffect (fn []
          (let [ctx (-> ref .-current (.getContext "2d"))
                draw-line (fn [drawing points]
                             (do (set! (.-lineCap ctx) "round")
                                 (set! (.-lineJoin ctx) "round")
                                 (if (= drawing "pen")
                                   (do (set! (.-lineWidth ctx) 5)
                                       (set! (.-globalCompositeOperation ctx) "source-over")
                                       (set! (.-strokeStyle ctx) central/Constants.Colors.Text.base))
                                   (do (set! (.-lineWidth ctx) 30)
                                       (set! (.-globalCompositeOperation ctx) "destination-out")
                                       (set! (.-strokeStyle ctx) "rgba(255,255,255,1)")))
                                 (dorun
                                   (map
                                     (fn [[from to]]
                                         (.beginPath ctx)
                                         (.moveTo ctx (:x from) (:y from))
                                         (.lineTo ctx (:x to) (:y to))
                                         (.stroke ctx))
                                     (map vector points (rest points))))))
                save (fn [] (re-frame/dispatch [::events/save-note @title @body]))
                add-point (fn [e]
                            (if (.-isPrimary e)
                              (let [canvas (.-current ref)
                                    rect (.getBoundingClientRect canvas)
                                    scale-x (/ (.-width canvas) (.-width rect))
                                    scale-y (/ (.-height canvas) (.-height rect))
                                    x (int (* (- (.-clientX e) (.-left rect)) scale-x))
                                    y (int (* (- (.-clientY e) (.-top rect)) scale-y))
                                    last (last @points)]
                                  (if (or (nil? last) (> (abs (- (:x last) x)) 1) (> (abs (- (:y last) y)) 1))
                                    (do (reset! points (concat @points [{:x x :y y}]))
                                        (draw-line drawing @points))))))
                draw (fn [e]
                       (if @pressed
                         (add-point e)))
                mousedown (fn [e]
                  (if (.-isPrimary e)
                      (do (reset! pressed (not (= (.-pointerType e) "touch")))
                          (draw e))))
                touch (fn [e] (if @pressed (.preventDefault e)))
                mouseup (fn [e] (if @pressed
                  (do (set! (-> js/document .-body .-style .-userSelect) "auto")
                      (reset! pressed false)
                      (reset! body (assoc @body :lines (concat (:lines @body) [{:drawing drawing :points @points}])))
                      (save)
                      (reset! points [])
                      (.clearRect ctx 0 0 (-> ref .-current .-width) (-> ref .-current .-height))
                      (dorun (map (fn [line] (draw-line (:drawing line) (:points line))) (:lines @body))))))
                scroll (fn [e]
                  (let [screen-height (-> js/window .-screen .-height)
                        canvas-height (:height @body)
                        y (.-scrollY js/window)]
                    (if (>= y (- canvas-height screen-height))
                      (do (reset! body (assoc @body :height (+ canvas-height screen-height)))
                          (reset! height (+ canvas-height screen-height))
                          (save)))))
                mount (fn []
                        (do (set! (-> ref .-current .-width) (:width @body))
                            (set! (-> ref .-current .-height) (:height @body))
                            (set! (-> ref .-current .-style .-width) (str (:width @body) "px"))
                            (set! (-> ref .-current .-style .-height) (str (:height @body) "px"))
                            (dorun (map (fn [line] (draw-line (:drawing line) (:points line))) (:lines @body)))))]

            (do (mount)
                (js/document.addEventListener "pointermove" draw (clj->js {:passive true}))
                (js/document.addEventListener "pointerdown" mousedown (clj->js {:passive true}))
                (js/document.addEventListener "touchstart" touch (clj->js {:passive false}))
                (js/document.addEventListener "pointerup" mouseup (clj->js {:passive true}))
                (js/document.addEventListener "scroll" scroll (clj->js {:passive true}))
                (fn [] (do (js/document.removeEventListener "pointermove" draw)
                           (js/document.removeEventListener "pointerdown" mousedown)
                           (js/document.removeEventListener "touchstart" touch)
                           (js/document.removeEventListener "pointerup" mouseup)
                           (js/document.removeEventListener "scroll" scroll)))))))

      [:canvas {:class (canvas-style) :ref ref :width @width :height @height}])))

(defn build []
  (let [selected @(re-frame/subscribe [::subs/selected])]
    (if (not (= (:title selected) @title))
      (do (if (and (not (nil? @title)) (not (string? (:body selected)))) (-> js/window .-location .reload))
          (reset! title (:title selected))
          (reset! body (:body selected))
          (reset! width (:width @body))
          (reset! height (:width @body))
          (reset! pressed false)
          (reset! points [])))
    (if (not (string? @body)) [:f> canvas-editor] [text-editor])))
