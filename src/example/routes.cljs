(ns example.routes
  (:require
    [example.subs :as subs]
    [example.events :as events]
    [example.effects]
    [example.views :as views]
    [re-frame.core :as re-frame]
    [reitit.core :as r]
    [reitit.coercion.spec :as rss]
    [reitit.frontend :as rf]
    [reitit.frontend.controllers :as rfc]
    [reitit.frontend.easy :as rfe]))

(defn href
  "Return relative url for given route. Url can be used in HTML links."
  ([k]
   (href k nil nil))
  ([k params]
   (href k params nil))
  ([k params query]
   (rfe/href k params query)))

;; TODO: remove this home-page and use from views.cljs
(defn home-page []
  [:div
   [:h1 "This is home page"]
   [:button
    ;; Dispatch navigate event that triggers a (side)effect.
    {:on-click #(re-frame/dispatch [::events/navigate ::sub-page2])}
    "Go to sub-page 2"]])

(def routes
  ["/"
   [""
    {:name      ::home
     :view      home-page
     :link-text "Home"
     :controllers
     [{;; Do whatever initialization needed for home page
       ;; I.e (re-frame/dispatch [::events/load-something-with-ajax])
       :start (fn [& params](js/console.log "Entering home page"))
       ;; Teardown can be done here.
       :stop  (fn [& params] (js/console.log "Leaving home page"))}]}]
   ["sub-page1"
    {:name      ::sub-page1
     :view      views/sub-page1
     :link-text "Sub page 1"
     :controllers
     [{:start (fn [& params] (js/console.log "Entering sub-page 1"))
       :stop  (fn [& params] (js/console.log "Leaving sub-page 1"))}]}]
   ["sub-page2"
    {:name      ::sub-page2
     :view      views/sub-page2
     :link-text "Sub-page 2"
     :controllers
     [{:start (fn [& params] (js/console.log "Entering sub-page 2"))
       :stop  (fn [& params] (js/console.log "Leaving sub-page 2"))}]}]])

(defn on-navigate [new-match]
  (let [old-match (re-frame/subscribe [::subs/current-route])]
    (when new-match
      (let [cs (rfc/apply-controllers (:controllers @old-match) new-match)
            m  (assoc new-match :controllers cs)]
        (re-frame/dispatch [::events/navigated m])))))

(def router
  (rf/router
    routes
    {:data {:coercion rss/coercion}}))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
    router
    on-navigate
    {:use-fragment true}))

(defn nav [{:keys [router current-route]}]
  (into
    [:ul]
    (for [route-name (r/route-names router)
          :let       [route (r/match-by-name router route-name)
                      text (-> route :data :link-text)]]
      [:li
       (when (= route-name (-> current-route :data :name))
         "> ")
       [:a {:href (href route-name)} text]])))

(defn router-component [{:keys [router]}]
  (let [current-route @(re-frame/subscribe [::subs/current-route])]
    [:div
     [nav {:router router :current-route current-route}]
     (when current-route
       [(-> current-route :data :view)])]))
