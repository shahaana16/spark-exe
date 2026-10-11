using Microsoft.Web.WebView2.Core;
using Microsoft.Web.WebView2.WinForms;
using System;
using System.Drawing;
using System.IO;
using System.Windows.Forms;

namespace SparkDesktop;

internal static class Program
{
    [STAThread]
    private static void Main()
    {
        ApplicationConfiguration.Initialize();
        Application.Run(new SparkWindow());
    }
}

internal sealed class SparkWindow : Form
{
    private readonly WebView2 browser = new() { Dock = DockStyle.Fill };

    public SparkWindow()
    {
        Text = "Spark.exe ✦ The Comeback Lab";
        BackColor = Color.FromArgb(250, 248, 255);
        ClientSize = new Size(1240, 840);
        MinimumSize = new Size(360, 520);
        StartPosition = FormStartPosition.CenterScreen;
        Controls.Add(browser);
        Shown += async (_, _) => await StartAsync();
    }

    private async System.Threading.Tasks.Task StartAsync()
    {
        try
        {
            // Stable user data folder means study progress survives app updates and relocation.
            string data = Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.LocalApplicationData), "SparkExe", "WebView2");
            Directory.CreateDirectory(data);
            string www = Path.Combine(AppContext.BaseDirectory, "www");
            if (!File.Exists(Path.Combine(www, "index.html")))
                throw new FileNotFoundException("App content missing. Keep Spark.exe next to its www folder.");

            CoreWebView2Environment environment = await CoreWebView2Environment.CreateAsync(null, data);
            await browser.EnsureCoreWebView2Async(environment);
            browser.CoreWebView2.SetVirtualHostNameToFolderMapping(
                "spark.local", www, CoreWebView2HostResourceAccessKind.DenyCors);
            browser.CoreWebView2.Settings.IsStatusBarEnabled = false;
            browser.CoreWebView2.Settings.AreDefaultContextMenusEnabled = false;
            browser.CoreWebView2.NewWindowRequested += (_, args) =>
            {
                args.Handled = true;
            };
            browser.CoreWebView2.NavigationStarting += (_, args) =>
            {
                if (!Uri.TryCreate(args.Uri, UriKind.Absolute, out Uri? u) ||
                    !string.Equals(u.Host, "spark.local", StringComparison.OrdinalIgnoreCase))
                    args.Cancel = true;
            };
            browser.CoreWebView2.Navigate("https://spark.local/index.html");
        }
        catch (Exception ex)
        {
            MessageBox.Show(this,
                "Spark.exe could not start. Ensure Microsoft Edge WebView2 Runtime is installed, " +
                "and extract the entire Windows app ZIP before starting.\n\n" + ex.Message,
                "Spark.exe startup error", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }
}
