
(ns reagentdemo.intro
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.interop :refer-macros [.' .! fvar]]
            [reagent.debug :refer-macros [dbg println]]
            [clojure.string :as string]
            [reagentdemo.syntax :refer-macros [get-source]]
            [reagentdemo.page :refer [link title]]
            [reagentdemo.common :as common :refer [demo-component]]))

(defn simple-component []
  [:div
   [:p "我是一个组件!"]
   [:p.someclass
    "我有 " [:strong "粗体"]
    [:span {:style {:color "red"}} " 和红色 "] "的字."]])

(defn simple-parent []
  [:div
   [:p "我包含包含了 simple-component."]
   [simple-component]])

(defn hello-component [name]
  [:p "你好, " name "!"])

(defn say-hello []
  [hello-component "世界"])

(defn lister [items]
  [:ul
   (for [item items]
     ^{:key item} [:li "Item " item])])

(defn lister-user []
  [:div
   "这是个列表:"
   [lister (range 3)]])

(def click-count (atom 0))

(defn counting-component []
  [:div
   "这个 atom " [:code "click-count"] " 的值是: "
   @click-count ". "
   [:input {:type "button" :value "点我!"
            :on-click #(swap! click-count inc)}]])

(defn atom-input [value]
  [:input {:type "text"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn shared-state []
  (let [val (atom "foo")]
    (fn []
      [:div
       [:p "值现在是: " @val]
       [:p "从这里修改它: " [atom-input val]]])))

(defn timer-component []
  (let [seconds-elapsed (atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
      [:div
       "时间按秒流逝了: " @seconds-elapsed])))

(defn render-simple []
  (reagent/render-component [simple-component]
                            (.-body js/document)))

(def bmi-data (atom {:height 180 :weight 80}))

(defn calc-bmi []
  (let [{:keys [height weight bmi] :as data} @bmi-data
        h (/ height 100)]
    (if (nil? bmi)
      (assoc data :bmi (/ weight (* h h)))
      (assoc data :weight (* bmi h h)))))

(defn slider [param value min max]
  (let [reset (case param :bmi :weight :bmi)]
    [:input {:type "range" :value value :min min :max max
             :style {:width "100%"}
             :on-change #(swap! bmi-data assoc
                                param (-> % .-target .-value)
                                reset nil)}]))

(defn bmi-component []
  (let [{:keys [weight height bmi]} (calc-bmi)
        [color diagnose] (cond
                          (< bmi 18.5) ["orange" "过轻"]
                          (< bmi 25) ["inherit" "正常"]
                          (< bmi 30) ["orange" "超重"]
                          :else ["red" "obese"])]
    [:div
     [:h3 "BMI 计算器"]
     [:div
      "Height: " (int height) "cm"
      [slider :height height 100 220]]
     [:div
      "Weight: " (int weight) "kg"
      [slider :weight weight 30 150]]
     [:div
      "BMI: " (int bmi) " "
      [:span {:style {:color color}} diagnose]
      [slider :bmi bmi 10 50]]]))

(defonce funmap (-> "reagentdemo/intro.cljs" get-source common/fun-map))
(defonce src-for (partial common/src-for funmap))

(defn intro []
  (let [github {:href "https://github.com/holmsand/reagent"}
        clojurescript {:href "https://github.com/clojure/clojurescript"}
        react {:href "http://facebook.github.io/react/"}
        hiccup {:href "https://github.com/weavejester/hiccup"}
        dynamic-children {:href "http://facebook.github.io/react/docs/multiple-components.html#dynamic-children"}]
    [:div.demo-text

     [:h2 "介绍一下 Reagent"]

     [:p [:a github "Reagent"] " 在
     " [:a clojurescript "ClojureScript"] " 和 " [:a
     react "React"] " 提供了精简的接口. 它帮助你快速地定义 React 组件,
     它单纯使用的是 ClojureScript 函数和数据类型,
     能够用类似 " [:a hiccup "Hiccup"] " 的语法来描述组件."]

     [:p "Reagent 的目标是实现快速定义任意复杂的界面, 而只依靠很少的基本概念,
     同时默认的性能足够好, 不需要额外去操心."]

     [:p "一个非常基础的 Reagent 组件看起是这样: "]
     [demo-component {:comp simple-component
                      :src (src-for [:simple-component])}]

     [:p "你可以把其他组件作为积木来创造新的组件, 比如:"]
     [demo-component {:comp simple-parent
                      :src (src-for [:simple-parent])}]

     [:p "数据通过 Clojure 原本就有的基本数据类型传递到子组件, 比如:"]

     [demo-component {:comp say-hello
                      :src (src-for [:hello-component :say-hello])}]

     [:p [:strong "注意: "]
      "在上面的例子, " [:code "hello-component"] " 大概可以同时被认为是普通的
      Clojure 函数, 而不是一个特殊的 Reagent 组件, 或者说, 可以写成圆括号而不使用花括号.
      唯一的差别会是性能, 因为`真正`的 Reagent 组件只会在数据改变时重新渲染.
      更加复杂的组件(见下文)必须使用方括号来调用."]

     [:p "这是另一个例子, 把列表元素显示为 "
     [:code "seq"] ":" ]

     [demo-component {:comp lister-user
                      :src (src-for [:lister :lister-user])}]

     [:p [:strong "Note: "]
     "上面的 " [:code "^{:key item}"] " 在这个简单的例子里并不是非常必要,
     不过在列表的每个动态生成的组件上附加一个唯一的 key 是一个好的实践,
     能够帮助 React 提升大列表的性能. key 可以用 meta-data (就像前面这个例子), 或者作为"
     [:code ":key"] " 元素设置在组件的第一个参数(需要是 map)上.
     阅读 React " [:a dynamic-children "文档"] "了解更多."]]))

(defn managing-state []
  [:div.demo-text
   [:h2 "Reagent 的管理状态"]

   [:p "Reagent 管理状态最简单的办法是用 Reagent 自带的版本的"
   [:code "atom"] ". 它和在 clojure.core 里的 atom 运行起来一样,
   除了它会记录它的每次 deref(解引用). 每个用了 " [:code "atom"]
   " 的组件在它的值改变时都会自动重新渲染."]

   [:p "我们用一个简单的例子演示一下:"]
   [demo-component {:comp counting-component
                    :src (src-for [:ns :click-count :counting-component])}]

   [:p "有时你要在一个组件局部维护状态.
   这种情况用 " [:code "atom"] " 处理起来也容易."]

   [:p "这是一个例子, 每次调用 "
    [:code "setTimeout"] " 组件就会被渲染然后更新计数器:"]

   [demo-component {:comp timer-component
                    :src (src-for [:timer-component])}]

   [:p "前面这个例子也用到了 Reagent 另一个功能:
   一个组件的函数可以返回另一个函数, 然后用于实际的渲染当中.
   这个函数和第一个函数用相同的参数去调用."]

   [:p "这为新创建的组件做初始化提供了方便, 而不用依赖 React 的生命周期事件."]

   [:p "通过把 "[:code "atom"]" 进行传递, 你就可以共享组件之间的状态管理, 比如:"]

   [demo-component {:comp shared-state
                    :src (src-for [:ns :atom-input :shared-state])}]

   [:p [:strong "注意: "] "组件函数可以不加参数直接调用, 只要它们是不可变的.
   你 "[:strong "也许也可以"]" 使用可变的对象, 但那样你需要去保证数据改变时组件也改变.
   Reagent 默认会假定两个引用相同的对象, 它们就是相等的."]])

(defn essential-api []
  [:div.demo-text
   [:h2 "基础 API"]

   [:p "Reagent 支持大多数 React 的 API, 不过但与绝大多数应用,
   真的仅仅一个调用的入口会被必须被用到: "
    [:code "reagent.core/render-component"] "."]

   [:p "它接收两个参数: 一个组件, 一个 DOM 节点. 比如,
   启动整个页面上第一个例子是这样写:"]

   [demo-component {:src (src-for [:ns :simple-component :render-simple])}]])

(defn performance []
  [:div.demo-text
   [:h2 "性能"]

   [:p "React 本身很快, 所以 Reagent 也是. 实际上, Reagent
   大部分时间会比一般的 React 还要快, 这要感谢 ClojureScript 实现的优化."]

   [:p "已经挂载的组件只有在它们的数据改变时才会重新渲染.
   这个改变可以来自 deref(解引用)的"
   [:code "atom"] ", 传递给组件的参数, 或者组件状态."]

   [:p "这里所有的修改的检查都是通过 "
   [:code "identical?"] " 函数, 仅仅是一次指针的对比, 因而开销非常低.
   作为参数传给组件的 Map 也是通过这个办法对比的:
   它们所有的 entry 是一致的, 那么它们就是相等的.
   这同样适用于内置的 React 组件比如 " [:code ":div"] ", " [:code ":p"] ", 等等."]

   [:p "所有这些意味着你可以直接绝大部分时间不去关心性能.
   你怎么想就怎么定义你的界面, 它总是会足够快的."]

   [:p "尽管如此, 有些场景还是需要留意一下. 如果你让 Reagent 去渲染一个巨大的组件的
    " [:code "seq"] ", 你需要给每个元素提供对应的 " [:code ":key"] " 属性,
    用来提升渲染的性能(见上文). 同时注意匿名函数通常来说不会相等,
    即便它们表示的是相同的代码相同的闭包."]

   [:p "不过再次强调, 通常情况你只需要相信 React 和 Reagent 会足够快.
   这个特别的页面是用一个单一的 Reagent 组件, 由上千个子组件组成的,
   (代码当中每个圆括号之类的地方都算一个独立的组件),
   而且这个页面可以在每秒更新更多次, 而不会给浏览器增加哪怕一点负担."]

   [:p "顺带说一下, 这个页面还用到了另一个 React 的花招:
   整个页面同时也用 Node 以及 "
   [:code "reagent/render-component-to-string"] " 做了预渲染.
   当它被浏览器加载时, React 在已经需在的 DOM 树上自动加上事件处理器."]])

(defn bmi-demo []
  [:div.demo-text
   [:h2 "集合到一起"]

   [:p "这是一个稍微不那么人为设计的例子: 简单的 BMI 计算器."]

   [:p "数据储存在单个 " [:code "reagent.core/atom"] ": 一个 map,
   包含 height, weight 和 BMI 作为 key."]

   [demo-component {:comp bmi-component
                    :src (src-for [:ns :bmi-data :calc-bmi :slider
                                   :bmi-component])}]])

(defn complete-simple-demo []
  [:div.demo-text
   [:h2 "Complete demo"]

   [:p "Reagent 项目包含几个完整的例子, 包括
   Leiningen 项目文件和全部代码. 这是其中的一个:"]

   [demo-component {:comp simpleexample/simple-example
                    :complete true
                    :src (-> "simpleexample.cljs"
                             get-source
                             common/syntaxify)}]])

(defn todomvc-demo []
  [:div.demo-text
   [:h2 "Todomvc"]

   [:p "Todolist 是 MVC 方案必须有的例子,
   Reagent 里的大致上是这样(算是有点作弊, 跳过了路由和数据持久化):"]

   [demo-component {:comp todomvc/todo-app
                    :complete true
                    :src (-> "todomvc.cljs"
                             get-source
                             common/syntaxify)}]])

(defn main []
  (let [show-all (atom false)
        head "Reagent: 给 ClojureScript 的极简的 React"]
    (js/setTimeout #(reset! show-all true) 500)
    (fn []
      [:div.reagent-demo
       [title head]
       [:h1 head]
       [intro]
       [managing-state]
       [essential-api]
       [bmi-demo]
       [performance]
       ;; Show heavy examples on load, to make html file smaller
       (when @show-all [complete-simple-demo])
       (when @show-all [todomvc-demo])])))
