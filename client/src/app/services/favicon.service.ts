import { Injectable, Renderer2, RendererFactory2 } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class FaviconService {
  private renderer: Renderer2;

  constructor(private rendererFactory: RendererFactory2) {
    this.renderer = this.rendererFactory.createRenderer(null, null);
    this.initFavicon();
    this.watchThemeChanges();
  }

  private setFavicon(theme: 'light' | 'dark') {
    const faviconPath = `favicon-${theme}.svg`;
    const favicon = document.getElementById('appFavicon') as HTMLLinkElement;

    if (favicon) {
      this.renderer.setAttribute(favicon, 'href', faviconPath);
    }
  }

  private getCurrentTheme(): 'light' | 'dark' {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  private initFavicon() {
    const currentTheme = this.getCurrentTheme();
    this.setFavicon(currentTheme);
  }

  private watchThemeChanges() {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    mediaQuery.addEventListener('change', () => {
      const newTheme = mediaQuery.matches ? 'dark' : 'light';
      this.setFavicon(newTheme);
    });
  }
}
