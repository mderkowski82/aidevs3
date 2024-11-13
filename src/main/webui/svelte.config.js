import adapter from '@sveltejs/adapter-static';

// In projects using SvelteKit v2
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte'

/** @type {import('@sveltejs/kit').Config} */
const config = {
	preprocess: vitePreprocess(),
	kit: {
		adapter: adapter({
			fallback: 'index.html'
		}),
		// Mark path non-relative, otherwise SvelteKit assumes it works in a sub-directory
		paths: {
			relative: false
		}
	}
};

export default config;