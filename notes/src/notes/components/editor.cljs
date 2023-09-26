(ns notes.components.editor
  (:require
    [reagent.core :as r]
    [re-frame.core :as re-frame]
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

(def title (r/atom ""))
(def body (r/atom ""))
(defn build [selected]
  (let [sync (fn []
    (if (not (= @title (:title selected))) (do (reset! title (:title selected)) (reset! body (:body selected)))))]
    (do
      (sync)
      (textbox {:value @body :on-change #(reset! body (-> % .-target .-value))}))))
