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
