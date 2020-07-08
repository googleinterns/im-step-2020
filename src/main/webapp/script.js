var clicked = false;

function getVideoResults() {
  var i;
  if (clicked == false) {
      fetch('/videoResults').then(response => response.json()).then((data) => {
      const dataListElement = document.getElementById('videoResults');
      dataListElement.innerHTML = '';
      console.log(dataListElement.innerHTML);
      if (dataListElement.innerHTML == '') {
        for (i=0; i<data.length; i++) {
          dataListElement.appendChild(
            createIFrame(data[i]));
        }
      }
    });
  }
  clicked = true;
}

function createIFrame(text) {
  const liElement = document.createElement('iframe');
  liElement.src = text;
  return liElement;
}