document.getElementById('load').addEventListener('click', async () => {
  const response = await fetch('/api/data');
  const text = await response.text();
  document.getElementById('output').innerText = text;
});