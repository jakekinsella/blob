(ns notes.components.editor
  (:require
    [reagent.core :as r]
    [re-frame.core :as re-frame]
    [react :as react]
    [spade.core :refer [defclass]]
    [central :as central]
    [notes.events :as events]))

(defclass textbox-style []
  {:width "100%"
   :height "93%"
   :padding-left "10px"
   :padding-right "10px"
   :border "none"
   :font-size "15px"
   :font-family "'Roboto', sans-serif"
   :font-weight "100"
   :user-select "none"}
  [:&:hover {:outline "none"}])
(defn textbox [attrs]
  [:textarea (merge-with + {:class (textbox-style) :required true} attrs)])

(defonce title (r/atom ""))
(defonce body (r/atom ""))
(defonce touched (r/atom false))

(defn build [selected]
  (let [sync (fn []
               (if (not (= @title (:title selected))) (do (reset! title (:title selected)) (reset! body (:body selected)))))
        save (fn [] (if @touched (do (reset! touched false) (re-frame/dispatch [::events/save-note @title @body]))))]
    (do
      (react/useEffect (fn [] (js/setInterval save 3000) (fn [] (js/clearInterval save))))
      (sync)
      (textbox {:value @body :on-change (fn [event] (do (reset! body (-> event .-target .-value)) (reset! touched true)))}))))
