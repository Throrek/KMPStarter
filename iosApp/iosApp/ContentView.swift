import SwiftUI
import StarterShared

struct ContentView: View {
    var body: some View {
        SharedView().ignoresSafeArea()
    }
}

private struct SharedView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
