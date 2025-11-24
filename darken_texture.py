import tkinter as tk
from tkinter import ttk, messagebox
from PIL import Image, ImageTk, ImageEnhance
import os

# Path to the texture
TEXTURE_PATH = os.path.join("src", "main", "resources", "assets", "create_sdt", "textures", "block", "deployer.png")

class DarkenerApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Texture Darkener - deployer.png")
        
        if not os.path.exists(TEXTURE_PATH):
            messagebox.showerror("Error", f"File not found: {TEXTURE_PATH}")
            root.destroy()
            return

        try:
            self.original_image = Image.open(TEXTURE_PATH).convert("RGBA")
        except Exception as e:
            messagebox.showerror("Error", f"Failed to load image: {e}")
            root.destroy()
            return

        self.current_image = self.original_image.copy()
        self.tk_image = None # Keep reference
        
        self.setup_ui()
        self.update_preview()

    def setup_ui(self):
        main_frame = ttk.Frame(self.root, padding="10")
        main_frame.pack(fill=tk.BOTH, expand=True)

        # Image Preview Area
        preview_frame = ttk.Frame(main_frame)
        preview_frame.pack(fill=tk.BOTH, expand=True, pady=10)

        # Original Image Label
        ttk.Label(preview_frame, text="Original").grid(row=0, column=0, padx=5)
        self.original_label = ttk.Label(preview_frame)
        self.original_label.grid(row=1, column=0, padx=5)
        
        # Show original
        orig_tk = ImageTk.PhotoImage(self.resize_for_preview(self.original_image))
        self.original_label.configure(image=orig_tk)
        self.original_label.image = orig_tk # Keep reference

        # Modified Image Label
        ttk.Label(preview_frame, text="Modified").grid(row=0, column=1, padx=5)
        self.modified_label = ttk.Label(preview_frame)
        self.modified_label.grid(row=1, column=1, padx=5)

        # Controls
        controls_frame = ttk.Frame(main_frame)
        controls_frame.pack(fill=tk.X, pady=10)

        ttk.Label(controls_frame, text="Brightness Factor:").pack(side=tk.LEFT)
        
        self.brightness_var = tk.DoubleVar(value=1.0)
        self.slider = ttk.Scale(controls_frame, from_=0.0, to=1.5, variable=self.brightness_var, command=self.update_preview)
        self.slider.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=10)
        
        self.value_label = ttk.Label(controls_frame, text="1.00")
        self.value_label.pack(side=tk.LEFT, padx=5)

        # Save Button
        save_btn = ttk.Button(main_frame, text="Save & Overwrite", command=self.save_image)
        save_btn.pack(pady=10)

    def resize_for_preview(self, img):
        # Scale up small textures for better visibility
        return img.resize((img.width * 8, img.height * 8), Image.NEAREST)

    def update_preview(self, val=None):
        factor = self.brightness_var.get()
        self.value_label.configure(text=f"{factor:.2f}")

        enhancer = ImageEnhance.Brightness(self.original_image)
        self.current_image = enhancer.enhance(factor)
        
        self.tk_image = ImageTk.PhotoImage(self.resize_for_preview(self.current_image))
        self.modified_label.configure(image=self.tk_image)

    def save_image(self):
        try:
            self.current_image.save(TEXTURE_PATH)
            messagebox.showinfo("Success", f"Image saved to:\n{TEXTURE_PATH}")
        except Exception as e:
            messagebox.showerror("Error", f"Failed to save image: {e}")

if __name__ == "__main__":
    root = tk.Tk()
    app = DarkenerApp(root)
    root.mainloop()
