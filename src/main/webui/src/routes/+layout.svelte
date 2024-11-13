<script lang="ts">
	import '../app.css';
	let { children } = $props();
	export const ssr = false;
</script>

<nav>
    <a href="/">home</a>
    <a href="/about">about</a>
</nav>

{@render children()}
