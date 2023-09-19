(ns notes.components.sidebar
  (:require
    [re-frame.core :as re-frame]
    [spade.core :refer [defclass]]
    [central :as central]))

(defclass pane-style [] {:min-width "250px" :z-index 10})
(defn pane [children] (into [:div {:class (pane-style)}] children))

(defclass pane-inner-style []
  {:min-width "250px"
   :max-width "250px"
   :min-height "100%"
   :max-height "100%"
   :position "fixed"
   :overflow-x "hidden"
   :overflow-y "scroll"
   :border-right (str "1px solid" central/Constants.colors.black)
   :background-color "white"
   :box-shadow (str "0px 0px 1px" central/Constants.colors.black)})
(defn pane-inner [children] (into [:div {:class (pane-inner-style)}] children))

(defclass container-style [] {:padding-top "14px" :padding-bottom "50px"})
(defn container [children]
  (into [:div {:class (container-style)}] children))

(defclass header-style [] {:padding-left "20px" :padding-top "5px" :padding-bottom "5px" :font-size "18px"})
(defn header [children]
  (into [:div {:class (header-style)}] children))

(defclass spacer-style [] {:width "100%" :height "8px"})
(defn spacer []
  [:div {:class (spacer-style)}])

(defclass item-style []
  {:display "block"
   :padding-left "30px"
   :text-decoration "none"
   :font-size "15px"
   :color central/Constants.colors.black
   :cursor "pointer"}
  [:&:hover {:text-decoration "none" :color "black"}]
  [:&:visited {:text-decoration "none"}]
  [:&:active {:text-decoration "none"}])
(defn item [attrs children]
  (into [:a (merge-with + attrs {:class (item-style)})]
    children))

(defn build [notes]
  (pane [(pane-inner [(container [(header "Notes")
                                (spacer)
                                [:div (item {:href "#"} ["test"])]])])]))
