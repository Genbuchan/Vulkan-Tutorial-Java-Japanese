# 基本のコード

## 目次

- [一般的な構造](#一般的な構造)
- [リソース管理](#リソース管理)
- [GLFWとの統合](#GLFWとの統合)

## 一般的な構造

本章では、次のコードでスタートします。これが今後の基本的なコードの原型となります。

```java

package javavulkantutorial;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Ch00BaseCode {

    private static class HelloTriangleApplication {

        public void run() {
            initWindow();
            initVulkan();
            mainLoop();
            cleanup();
        }

        private void initWindow() {

        }

        private void initVulkan() {

        }

        private void mainLoop() {

        }

        private void cleanup() {

        }

    }

    public static void main(String[] args) {

        HelloTriangleApplication app = new HelloTriangleApplication();
        
        app.run();

    }
}

```

はじめに、LWJGLからインポートしたいものがあります。それは、`GLFW`クラスと、その中に含まれるメソッド、列挙型の定数、そして`MemoryUtil`の`NULL`です。LWJGLでは独自のヌルポインタを扱うことがあるので、それを扱うために`NULL`をインポートしました。

`Ch00BaseCode`クラスの中に、Vulkanオブジェクトである`HelloTriangleApplication`クラスをプライベートメンバとして定義します。そのクラスでは、以下のメソッドを定義しています。

- `initWindow`
- `initVulkan`
- `mainLoop`
- `cleanup`

さて、処理をおおまかに見てみましょう。

まず、`initWindow`メソッドで初期化処理を行います。

`mainLoop`メソッドでは、ウィンドウが閉じられるまで繰り返し行う処理を実行します。

ウィンドウが閉じて`mainLoop`メソッドの処理が終了したら、`cleanup`メソッドを呼び出し、使用したリソースを確実に解放します。

実行中に何らかの致命的なエラーが発生した場合、それを説明する例外をスローしておきます。この例外は`Ch00BaseCode`クラスの`main`メソッドに伝えられます。

もしプログラムをターミナルから実行しているのであれば、これらの例外はターミナル等の標準出力にも伝えられるはずです。

この章以降の全ての章では、`initVulkan`メソッドで呼び出す新しいメソッドと、1つ以上の新しいVulkanオブジェクトが作成されます。そして、作成されたVulkanオブジェクトは、プログラムの最後に`cleanup`メソッドで解放する必要があります。

## リソース管理

### C++とJavaのメモリ管理の違い

C++では、明示的にメモリ管理を行う場合、`malloc`などでメモリを動的に割り当てます。

おっと、割り当てっぱなしはいけませんね。ちゃんと掃除は必要です。そこで、割り当てたメモリを解放するために`free`関数を呼び出す必要があります。

一方、Javaにはガベージコレクションが存在するため、プログラマは原則としてメモリ管理を意識する必要はありません。

しかし、LWJGLでバインディングされる各種APIは本来C++のエコシステムのものであり、故にJavaにおける暗黙的なメモリ管理と、C++における明示的なメモリ管理は大きく異なります。

これでは、適切なメモリ管理を行えそうにもありませんね。

そこで、LWJGLに備えられた[独自のメモリ管理システム](https://github.com/LWJGL/lwjgl3-wiki/wiki/1.3.-Memory-FAQ)を使用することにより、これを解決することができます。どうしてもC++ライクな方法で明示的にメモリ管理を行う必要がある場合、LWJGLの`MemoryUtil`クラスに備えられた機能が役立つでしょう。

しかし、明示的にメモリを管理するということは、メモリの割り当てと解放を適切に行わなければならない、という責任を負うことを意味します。

### チュートリアルにおけるメモリ管理の方向性

本チュートリアルでは、Vulkanオブジェクトのメモリの割り当てと解放を明示的に行なうことにしました。

Vulkanのニッチな点は、設計上の間違いを避けるために全ての操作を明示的に行うことです。故に、APIがどのように動作するかを学ぶためには、オブジェクトの寿命を明らかにすることは良いことだと言えます。そのために、メモリの割り当てと解放を明示的に行うわけです。

なので、Javaらしいメモリ管理と言えない方法を使うことになるかもしれません。本当にすみません。

もっとも、本番のコードでは無理してメモリ管理を必要はないですし、Javaが持つ優秀なガベージコレクションと、LWJGLのメモリ管理システムの力を借りながらメモリ管理を行なっていく方が、結局のところ楽になると思います。

### Vulkanにおけるオブジェクトの取り扱い

Vulkanのオブジェクトは、`vkCreateXXX`メソッドで直接作成されるもの、`vkAllocateXXX`メソッドで別のオブジェクトを介して割り当てられるものがあります。

オブジェクトがどこにも使用されていないことを確認した後、これに対応する`vkDestroyXXX`メソッドや、`vkFreeXXX`メソッドでオブジェクトを破棄する必要があります。

これらのメソッドのパラメータは、オブジェクトの種類によって異なりますが、実は全てのメソッドに共通するパラメータが1つだけ存在します。これはあくまでもオプションなのですが、カスタムメモリアロケータのコールバックを指定することができるのです。

しかし、本チュートリアルでは、このパラメータは使いません。代わりに、空のポインタを指す`MemoryUtil`クラスの`NULL`を引数に渡しておきましょう。そのために、以下のNULLを事前にインポートしました。

```java

import static org.lwjgl.system.MemoryUtil.NULL;

```

## GLFWとの統合

Vulkanを使用する場合、オフスクリーンレンダリングを使用すれば、ウィンドウを作成せずとも完全に機能します。…でもやっぱり、何か表示された方が面白いですよね！

そこで、GLFWを使いましょう。1行目の`package`構文を改行し、以下のコードを記述してください。

```java

import static org.lwjgl.glfw.GLFW.*;

```

こうすれば、GLFWに含まれる定義と、Vulkanのヘッダが自動で読み込まれます。続いて、`initWindow`メソッドを定義し、`run`メソッドの中で先に呼び出しておきましょう。

ここでの`initWindow`関数の役割は、GLFWを初期化し、ウィンドウを作成することです。

```java

public void run() {
    initWindow();
    mainLoop();
    cleanup();
}

```

```java

private void initWindow() {

}

```

`initWindow`メソッドで最初に呼び出すのは、GLFWライブラリを初期化する`glfwInit`メソッドでなければなりません。

GLFWは、元々OpenGLのコンテキストを作成するために設計されているので、その後に呼び出すメソッドでOpenGLのコンテキストを作成しないように教えてあげる必要があります。

そのメソッドが以下です。

```java

glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

```

サイズが変更されたウィンドウを扱うには、特別な処理が必要になります。とりあえず、今はこれを無効にしておきましょう。

`glfwWindowHint`メソッドをもう1回呼び出して、ウィンドウのリサイズを無効化するように教えてあげます。

```java

glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

```

お待たせしました。これからウィンドウを作っていきましょう。まず、`glfwCreateWindow`メソッドを呼び出し、ウィンドウを初期化します。

このメソッドの戻り値は、これからウィンドウの内容を操作するのに必要な「ウィンドウハンドラ」を参照するための数値です。`long`型になっています。

戻り値は`window`変数に格納しておきましょう。

さあ、ここまでの手順に沿って、一気にコードを書き換えていきましょう。

```java

package javavulkantutorial;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Ch00BaseCode {

    private static class HelloTriangleApplication {

        // ウィンドウの横幅
        private static final int WIDTH = 800;
        // ウィンドウの高さ
        private static final int HEIGHT = 600;

        // ウィンドウハンドラ
        private long window;

        public void run() {
            initWindow();
            initVulkan();
            mainLoop();
            cleanup();
        }

        private void initWindow() {

            // GLFWを初期化
            if (!glfwInit()) {
                // falseが返ってきた(失敗した)場合、例外をスローしてGLFWの初期化に失敗したことを伝える
                throw new RuntimeException("GLFWの初期化に失敗しました");
            }

            // GLFWが作るウィンドウに役割を教える
            glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

            //ウィンドウのタイトルをHelloTriangleApplicationと同じ名前にする
            String title = getClass().getEnclosingClass().getSimpleName();

            // ウィンドウを作成し、そのハンドラの参照をwindow変数に格納する
            window = glfwCreateWindow(WIDTH, HEIGHT, title, NULL, NULL);

            // 何らかの原因でwindow変数にNULLが代入された(失敗した)場合、例外をスローしてウィンドウの作成に失敗したことを伝える
            if (window == NULL) {
                throw new RuntimeException("ウィンドウの作成に失敗しました");
            }

        }

        private void initVulkan() {

        }

        private void mainLoop() {

            // ウィンドウが閉じるまでループする処理
            while (!glfwWindowShouldClose(window)) {
              // イベントキューに入っている保留中のイベントを処理する
              glfwPollEvents();
            }

        }

        private void cleanup() {

            // ウィンドウを破棄してクリーンアップする
            glfwDestroyWindow(window);
            
            // GLFW自体を終了する
            glfwTerminate();

        }

    }

    public static void main(String[] args) {

        HelloTriangleApplication app = new HelloTriangleApplication();
        
        app.run();

    }
}

```

では、追加した箇所の処理を追っていきましょう。

まず、`HelloTriangleApplication`クラスの中に、以下のフィールドができました。

```java

    // ウィンドウの横幅
    private static final int WIDTH = 800;
    // ウィンドウの高さ
    private static final int HEIGHT = 600;

    // ウィンドウハンドラ
    private long window;

```

2つの`int`型の定数である`WIDTH`と`HEIGHT`を定義しました。この定数は、作成するウィンドウの縦横のサイズを決めるものです。

ウィンドウのサイズは、とりあえず800×600としましょう。ちなみに、縦横比は4:3です。懐かしいですね。

次に、`long`型の`window`変数を定義しました。この変数は、先ほど解説したウィンドウハンドラの参照を入れておくためのものです。

ですが、今は変数を定義するだけにしておきましょう。値は後ほど格納します。

続いて、`initWindow`メソッドの処理を見てみましょう。

```java

private void initWindow() {

    // GLFWを初期化
    if (!glfwInit()) {
        // falseが返ってきた(失敗した)場合、例外をスローしてGLFWの初期化に失敗したことを伝える
        throw new RuntimeException("GLFWの初期化に失敗しました");
    }

    // GLFWが作るウィンドウに役割を教える
    glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

    //ウィンドウのタイトルをHelloTriangleApplicationと同じ名前にする
    String title = getClass().getEnclosingClass().getSimpleName();

    // ウィンドウを作成し、そのハンドラの参照をwindow変数に格納する
    window = glfwCreateWindow(WIDTH, HEIGHT, title, NULL, NULL);

    // 何らかの原因でwindow変数にNULLが代入された(失敗した)場合、例外をスローしてウィンドウの作成に失敗したことを伝える
    if (window == NULL) {
        throw new RuntimeException("ウィンドウの作成に失敗しました");
    }

}

```

まず最初に`glfwInit`メソッドを実行していますね。このメソッドは`boolean`型の値を返してきます。

このメソッドは、GLFWを初期化するものです。ウィンドウを出すにせよ、Vulkanで描いた画をそこに描画するにせよ、まずはGLFWを初期化しないと始まりませんよね。

これをif文の条件の中で実行し、`glfwInit`メソッドでGLFWの初期化が成功した場合に`true`が返ってくるところを、論理否定で`false`にひっくり返して、そのまま次の処理に進めるようにしています。

さて、問題はメソッドの戻り値が`false`だった場合、つまり初期化に失敗した場合です。このまま次の処理に進んではいけませんね。そのための処理を行っておきましょう。

```java

throw new RuntimeException("GLFWの初期化に失敗しました");

```

`RuntimeException`をスローし、GLFWの初期化に失敗した旨を伝えます。

さて、例外処理ができたところで、次にいきましょう。

```java

glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

```

先ほど解説した`glfwWindowHint`メソッドですね。このメソッドを使って、GLFWが作成するウィンドウにさまざまな役割を教えてあげるわけです。

今回は、コンテキストを作成するクライアントAPIが存在しないので、`GLFW_CLIENT_API`パラメータには`GLFW_NO_API`を渡しておきます。

ちなみにこれがOpenGLアプリケーションなら、`GLFW_OPENGL_API`を入れておくことになります。しかし、今回はVulkanアプリケーションなので、これに該当しませんね。

次に、`GLFW_RESIZABLE`パラメータです。これはウィンドウのサイズを変更できるかどうかを決めるのですが、ウィンドウのサイズを変更するためには特殊な処理が必要なので、いきなりそんな手間をかけるわけにもいきません。

よって、このパラメータには`GLFW_FALSE`を渡しておきます。これでウィンドウのサイズを変更できなくなります。

これでウィンドウの役割は決まりました。では、実際にウィンドウを作成していきましょう。

```java

String title = getClass().getEnclosingClass().getSimpleName();

window = glfwCreateWindow(WIDTH, HEIGHT, title, NULL, NULL);

```

`glfwCreateWindow`メソッドを呼び出し、先ほど決めたウィンドウのサイズと、タイトルをパラメータに渡します。

無事にウィンドウを作成できればいいのですが、何らかの原因でウィンドウの作成に失敗することも考えられます。その場合の処理を行っておきましょう。

```java

if(window == NULL) {
    throw new RuntimeException("ウィンドウの作成に失敗しました。");
}


```

何らかの原因で`glfwCreateWindow`メソッドの実行が失敗したとき、`NULL`が返ってきます。つまり、ウィンドウは作成されなかったわけです。これを`if`文で判定します。

`window`の値が`NULL`であった場合、`RuntimeException`をスローし、ウィンドウの作成に失敗したことを伝えます。

さて、ウィンドウの初期化ができたところで、次はメインループです。

```java

private void mainLoop() {

    while (!glfwWindowShouldClose(window)) {
        glfwPollEvents();
    }

}

```

これは、メインループを行う処理です。リアルタイムCGは1秒間に何度も画が描き直されるため、そのための処理をここでループすることになります。

```java

while (!glfwWindowShouldClose(window)) {
    // ...
}

```

`while`文で、`glfwWindowShouldClose`メソッドが`true`を返すまで、延々と処理をループさせることにします。

ところで、`glfwWindowShouldClose`メソッドが`true`を返すときは、一体どういうときなのでしょうか。

例えば、ユーザーが×ボタンを押してウィンドウを閉じたとき、何らかの原因でエラーが発生したときがそうですね。この場合、もちろんウィンドウは閉じられるべきなので、そのときに`true`を返すわけです。

`while`文は条件が`true`であれば処理がループされますよね。結果が`true`の値を論理反転すれば`false`になるため、これでループから抜けることができます。

では、`while`文の中ではどんな処理が走っているのでしょうか。

```java

glfwPollEvents();

```

はい、これだけです！今のところは。

`glfwPollEvents`メソッドは、プログラムの動作中に発生した様々なイベントを一時的に格納する「イベントキュー」に貯まった処理を実行するものです。

イベントにはいろいろなものがあります。例えば、マウスなどの入力が起きたとき、ウィンドウを移動したとき、ウィンドウのサイズを変更したとき、メニューの操作を行ったときなど、様々なものが含まれます。

これを毎回実行しておくことで、プログラムの動作中にこれらのイベントが発生した際に、適切なコールバック関数が実行されるようになります。特に何もイベントが発生していない場合は、そのまま処理が終わります。

では最後に、ウィンドウを閉じる際に必要な処理を見てみましょう。

```java

private void cleanup() {

    glfwDestroyWindow(window);

    glfwTerminate();
}

```

`glfwDestoyWindow`メソッドで、先ほど作成したウィンドウを破棄します。引数に`window`変数が渡されているのはそのためです。

後始末としてGLFWを終了する必要があるため、`glfwTerminate`メソッドを呼び出して、ちゃんとリソースを解放しましょう。

この一連の処理で、リソースの後始末ができるようになります。

さあ、これで全ての処理が出来上がりましたね。実行してみましょう。ウィンドウは表示されましたか？

おめでとうございます！これでVulkanアプリケーションを作るための下地ができました。

次の章でVulkanオブジェクトを作成し、Vulkanとはじめましての挨拶を交わしましょう。
