(ns example.core
  (:require
    [example.config :as config]
    [example.events :as events]
    [example.routes :as routes]
    [re-frame.core :as re-frame]
    [reagent.dom :as rdom]
    ))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (routes/init-routes!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [routes/router-component {:router routes/router}] root-el)))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
