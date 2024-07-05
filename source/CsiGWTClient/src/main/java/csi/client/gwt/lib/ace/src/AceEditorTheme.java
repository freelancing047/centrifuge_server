// Copyright (c) 2011 David H. Hovemeyer <david.hovemeyer@gmail.com>
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package csi.client.gwt.lib.ace.src;

/**
 * Enumeration for ACE editor themes.
 * Note that the corresponding .js file must be loaded
 * before a theme can be set.
 */
public enum AceEditorTheme {
	AMBIANCE("ambiance"), //$NON-NLS-1$
	CHAOS("chaos"), //$NON-NLS-1$
	CHROME("chrome"), //$NON-NLS-1$
	CLOUDS("clouds"), //$NON-NLS-1$
	CLOUDS_MIDNIGHT("clouds_midnight"), //$NON-NLS-1$
	COBALT("cobalt"), //$NON-NLS-1$
	CRIMSON_EDITOR("crimson_editor"), //$NON-NLS-1$
	DAWN("dawn"), //$NON-NLS-1$
	DREAMWEAVER("dreamweaver"), //$NON-NLS-1$
	ECLIPSE("eclipse"), //$NON-NLS-1$
	GITHUB("github"), //$NON-NLS-1$
	IDLE_FINGERS("idle_fingers"), //$NON-NLS-1$
	KR_THEME("kr_theme"), //$NON-NLS-1$
	KR("kr"), //$NON-NLS-1$
	MERBIVORE("merbivore"), //$NON-NLS-1$
	MERBIVORE_SOFT("merbivore_soft"), //$NON-NLS-1$
	MONO_INDUSTRIAL("mono_industrial"), //$NON-NLS-1$
	MONOKAI("monokai"), //$NON-NLS-1$
	PASTEL_ON_DARK("pastel_on_dark"), //$NON-NLS-1$
	SOLARIZED_DARK("solarized_dark"), //$NON-NLS-1$
	SOLARIZED_LIGHT("solarized_light"), //$NON-NLS-1$
	TERMINAL("terminal"), //$NON-NLS-1$
	TEXTMATE("textmate"), //$NON-NLS-1$
	TOMORROW_NIGHT_BLUE("tomorrow_night_blue"), //$NON-NLS-1$
	TOMORROW_NIGHT_BRIGHT("tomorrow_night_bright"), //$NON-NLS-1$
	TOMORROW_NIGHT_EIGHTIES("tomorrow_night_eighties"), //$NON-NLS-1$
	TOMORROW_NIGHT("tomorrow_night"), //$NON-NLS-1$
	TOMORROW("tomorrow"), //$NON-NLS-1$
	TWILIGHT("twilight"), //$NON-NLS-1$
	VIBRANT_INK("vibrant_ink"), //$NON-NLS-1$
	XCODE("xcode"); //$NON-NLS-1$
	
	private final String name;
	
	private AceEditorTheme(String name) {
		this.name = name;
	}
	
	/**
	 * @return the theme name (e.g., "eclipse")
	 */
	public String getName() {
		return name;
	}
}
