(ns rehook.dom)

(defn ->html
  ([$ e]
   (list $ e))
  ([$ e props]
   (list $ e props))
  ([$ e props & children]
   (apply list $ e props (map (fn [x]
                                (if (sequential? x)
                                  (apply ->html $ x)
                                  x))
                              children))))

(defmacro html [$ component]
  `~(apply ->html $ component))

(defmacro defui
  [name [ctx props $] & body]
  `(def ~name
     ^:rehook/component
     (fn [ctx# $#]
       (let [~ctx ctx#
             ~$ $#]
         (fn ~(gensym name) [props#]
           (let [~props props#]
             ~@body))))))

(defmacro ui
  [[ctx props $] & body]
  ^:rehook/component
  `(fn [ctx# $#]
     (let [~ctx ctx#
           ~$ $#]
       (fn [props#]
         (let [~props props#]
           ~@body)))))
