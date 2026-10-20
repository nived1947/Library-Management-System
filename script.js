const seedBooks = [
  {id:"BK-101",title:"Clean Code",author:"Robert C. Martin",topic:"Programming",copies:3,available:3},
  {id:"BK-102",title:"Introduction to Algorithms",author:"Thomas H. Cormen",topic:"Algorithms",copies:2,available:2},
  {id:"BK-103",title:"Database System Concepts",author:"Abraham Silberschatz",topic:"Database",copies:2,available:2},
  {id:"BK-104",title:"The Pragmatic Programmer",author:"Andrew Hunt",topic:"Software Engineering",copies:2,available:2},
  {id:"BK-105",title:"Python Crash Course",author:"Eric Matthes",topic:"Programming",copies:1,available:1}
];
let books = [...seedBooks], issued = [], waiting = {}, returnedStack = [];
const topicTree = {root:null};
function insertTopic(node, topic, book) {
  if (!node) return {topic, books:[book], left:null, right:null};
  const cmp = topic.toLowerCase().localeCompare(node.topic.toLowerCase());
  if (cmp===0) node.books.push(book); else if(cmp<0) node.left=insertTopic(node.left,topic,book); else node.right=insertTopic(node.right,topic,book);
  return node;
}
function buildTopicTree(){topicTree.root=null;books.forEach(b=>topicTree.root=insertTopic(topicTree.root,b.topic,b));}
function searchTopic(node, query, out=[]) {
  if(!node)return out;
  if(node.topic.toLowerCase().includes(query.toLowerCase()))out.push(...node.books);
  searchTopic(node.left,query,out);searchTopic(node.right,query,out);return out;
}
function queueCount(){return Object.values(waiting).reduce((n,q)=>n+q.length,0)}
function toast(msg){const t=document.querySelector('#toast');t.textContent=msg;t.classList.add('show');clearTimeout(window.toastTimer);window.toastTimer=setTimeout(()=>t.classList.remove('show'),2800)}
function empty(title,desc){return `<div class="empty"><strong>${title}</strong>${desc}</div>`}
function bookCard(b){return `<article class="book-card"><div class="book-cover">▤</div><h3>${escapeHtml(b.title)}</h3><p>${escapeHtml(b.author)}</p><span class="tag">${escapeHtml(b.topic)}</span></article>`}
function escapeHtml(v){return String(v).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]))}
function refresh(){
 document.querySelector('#stat-books').textContent=books.length;
 document.querySelector('#stat-issued').textContent=issued.length;
 document.querySelector('#stat-waiting').textContent=queueCount();
 document.querySelector('#stat-available').textContent=books.reduce((n,b)=>n+b.available,0);
 document.querySelector('#recent-books').innerHTML=books.slice(-3).reverse().map(bookCard).join('')||empty('No books yet','Add a book to get started.');
 renderBooks();renderSelects();renderWaiting();renderHistory();renderTopics();
}
function renderBooks(){
 const q=(document.querySelector('#book-search')?.value||'').toLowerCase();
 const filtered=books.filter(b=>[b.title,b.author,b.topic,b.id].some(x=>x.toLowerCase().includes(q)));
 document.querySelector('#book-count').textContent=`${filtered.length} title${filtered.length===1?'':'s'}`;
 document.querySelector('#book-table').innerHTML=filtered.map(b=>`<tr><td><strong>${escapeHtml(b.title)}</strong><small>${escapeHtml(b.id)}</small></td><td>${escapeHtml(b.author)}</td><td><span class="tag">${escapeHtml(b.topic)}</span></td><td><span class="pill ${b.available?'available':'unavailable'}">${b.available} / ${b.copies} available</span></td><td>${escapeHtml(b.id)}</td></tr>`).join('')||`<tr><td colspan="5">${empty('No matching books','Try a different search term.')}</td></tr>`;
}
function renderSelects(){
 const issue=document.querySelector('#issue-book'),ret=document.querySelector('#return-book');
 const oldIssue=issue.value,oldRet=ret.value;
 issue.innerHTML=books.map(b=>`<option value="${escapeHtml(b.id)}">${escapeHtml(b.title)} (${b.available} available)</option>`).join('');
 ret.innerHTML=issued.map(i=>`<option value="${escapeHtml(i.key)}">${escapeHtml(i.title)} — ${escapeHtml(i.reader)}</option>`).join('');
 if([...issue.options].some(o=>o.value===oldIssue))issue.value=oldIssue;
 if([...ret.options].some(o=>o.value===oldRet))ret.value=oldRet;
 if(!books.length)issue.innerHTML='<option value="">No books in catalogue</option>';
 if(!issued.length)ret.innerHTML='<option value="">No books currently issued</option>';
}
function renderWaiting(){
 const entries=Object.entries(waiting).filter(([,q])=>q.length);
 document.querySelector('#waiting-content').innerHTML=entries.length?entries.map(([id,q])=>{const b=books.find(x=>x.id===id);return `<article class="queue-card"><h3>${escapeHtml(b?.title||id)}</h3><p>${q.length} reader${q.length===1?'':'s'} waiting · FIFO queue</p>${q.map((p,i)=>`<div class="queue-person"><div><strong>${i+1}. ${escapeHtml(p.name)}</strong> <span>(${escapeHtml(p.studentId)})</span></div><span>${i===0?'Next in line':'Waiting'}</span></div>`).join('')}</article>`}).join(''):empty('No waiting lists','Readers will appear here when all copies of a book are issued.');
}
function renderHistory(){document.querySelector('#history-content').innerHTML=returnedStack.length?returnedStack.map((r,i)=>`<article class="queue-card"><h3>${i===0?'↙  ':''}${escapeHtml(r.title)}</h3><p>Returned by ${escapeHtml(r.reader)} · ${escapeHtml(r.date)} · ${i===0?'Most recent':'Earlier return'}</p></article>`).join(''):empty('No returns yet','Returned books will appear here, newest first (LIFO).')}
function renderTopics(){
 buildTopicTree();const q=document.querySelector('#topic-search').value.trim();
 let results=q?searchTopic(topicTree.root,q):books;
 document.querySelector('#topic-results').innerHTML=results.length?results.map(bookCard).join(''):empty('No topics found','Try another subject, such as Programming or Algorithms.');
}
function navigate(page){
 document.querySelectorAll('.page').forEach(p=>p.classList.add('hidden'));
 document.querySelector('#'+page).classList.remove('hidden');
 document.querySelectorAll('.nav-item').forEach(b=>b.classList.toggle('active',b.dataset.page===page));
 const titles={dashboard:'Dashboard',books:'All Books',issue:'Issue a Book',return:'Return a Book',waiting:'Waiting List',topics:'Search by Topic',history:'Return History'};
 document.querySelector('#page-title').textContent=titles[page]||'Dashboard';
 if(page==='books')renderBooks();
 if(page==='topics')renderTopics();
}
document.querySelectorAll('[data-page]').forEach(b=>b.addEventListener('click',()=>navigate(b.dataset.page)));
document.querySelectorAll('[data-goto]').forEach(b=>b.addEventListener('click',()=>navigate(b.dataset.goto)));
document.querySelector('#book-search').addEventListener('input',renderBooks);
document.querySelector('#topic-search').addEventListener('input',renderTopics);
const dialog=document.querySelector('#add-dialog');
document.querySelector('#open-add').addEventListener('click',()=>dialog.showModal());
document.querySelector('#close-add').addEventListener('click',()=>dialog.close());
document.querySelector('#add-form').addEventListener('submit',e=>{
 e.preventDefault();const id=document.querySelector('#new-id').value.trim();
 if(books.some(b=>b.id.toLowerCase()===id.toLowerCase())){toast('That book ID already exists.');return}
 const copies=Number(document.querySelector('#new-copies').value);
 books.push({id,title:document.querySelector('#new-title').value.trim(),author:document.querySelector('#new-author').value.trim(),topic:document.querySelector('#new-topic').value.trim(),copies,available:copies});
 e.target.reset();document.querySelector('#new-copies').value=1;dialog.close();refresh();toast('Book added to the catalogue.');
});
document.querySelector('#issue-form').addEventListener('submit',e=>{
 e.preventDefault();const id=document.querySelector('#issue-book').value,b=books.find(x=>x.id===id);
 if(!b){toast('Add a book to the catalogue first.');return}
 const name=document.querySelector('#reader-name').value.trim(),studentId=document.querySelector('#reader-id').value.trim();
 if(b.available>0){b.available--;issued.push({key:id+'-'+Date.now(),bookId:id,title:b.title,reader:name,studentId});toast(`${b.title} issued to ${name}.`)}
 else{waiting[id]??=[];if(waiting[id].some(p=>p.studentId.toLowerCase()===studentId.toLowerCase())){toast('This reader is already waiting for this book.');return}waiting[id].push({name,studentId});toast(`No copies available. ${name} joined the waiting list.`)}
 e.target.reset();refresh();
});
document.querySelector('#return-form').addEventListener('submit',e=>{
 e.preventDefault();const key=document.querySelector('#return-book').value;const idx=issued.findIndex(i=>i.key===key);
 if(idx<0){toast('Select an issued book to return.');return}
 const item=issued.splice(idx,1)[0],b=books.find(x=>x.id===item.bookId);
 returnedStack.unshift({title:item.title,reader:item.reader,date:new Date().toLocaleDateString()});
 if(waiting[item.bookId]?.length){const next=waiting[item.bookId].shift();issued.push({key:item.bookId+'-'+Date.now(),bookId:item.bookId,title:b.title,reader:next.name,studentId:next.studentId});toast(`${item.title} returned. Automatically issued to ${next.name} from the waiting list.`)}
 else{b.available++;toast(`${item.title} returned successfully.`)}
 refresh();
});
refresh();