/**
 * ============================================================
 * ICAROS — FRONTEND API REFERENCE SCRIPT
 * ============================================================
 * Guia completo de integração com todas as APIs do backend.
 * Use este arquivo como referência para construir o frontend.
 *
 * Stack sugerida: React + Vite + Axios + React Query + Tailwind CSS
 *
 * Base URL: https://sua-api.azurewebsites.net
 * Auth: JWT Bearer Token (RSA, obtido em POST /login)
 * ============================================================
 */

// ============================================================
// 1. CONFIGURAÇÃO BASE — api.js
// ============================================================
import axios from 'axios';

const BASE_URL = 'https://sua-api.azurewebsites.net';

const api = axios.create({ baseURL: BASE_URL });

// Injeta o JWT em todas as requisições autenticadas
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('icaros_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Redireciona para login em caso de token expirado
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('icaros_token');
      localStorage.removeItem('icaros_user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default api;


// ============================================================
// 2. AUTENTICAÇÃO — auth.js
// ============================================================

/**
 * POST /login
 * Body: { username: "email@ex.com", password: "senha" }
 * Retorna: { accessToken: "eyJ..." }
 */
export async function login(email, password) {
  const res = await api.post('/login', { username: email, password });
  const { accessToken } = res.data;
  localStorage.setItem('icaros_token', accessToken);
  // Decodifica o payload para pegar role e userId
  const payload = JSON.parse(atob(accessToken.split('.')[1]));
  localStorage.setItem('icaros_user', JSON.stringify({
    userId: payload.sub,
    email: payload.email,
    role: payload.scope ?? payload.role
  }));
  return payload;
}

export function logout() {
  localStorage.removeItem('icaros_token');
  localStorage.removeItem('icaros_user');
  window.location.href = '/login';
}

export function getCurrentUser() {
  const user = localStorage.getItem('icaros_user');
  return user ? JSON.parse(user) : null;
}

export function isMusician()  { return getCurrentUser()?.role === 'musician';  }
export function isProducer()  { return getCurrentUser()?.role === 'producer';  }
export function isListener()  { return getCurrentUser()?.role === 'lover';     }


// ============================================================
// 3. CADASTRO — register.js
// ============================================================

/**
 * POST /user  — Cadastrar MÚSICO (genérico)
 * Body: { name, cpf, email, password, musicalGenre: ["samba","funk"], role: "musician" }
 */
export async function registerMusician(data) {
  return api.post('/user', { ...data, role: 'musician' });
}

/**
 * POST /register/producer  — Cadastrar PRODUTOR (público)
 * Body: { name, cpf, email, password, genrePreferences: ["samba","pagode"] }
 * Role atribuída automaticamente como "producer"
 */
export async function registerProducer({ name, cpf, email, password, genrePreferences }) {
  return api.post('/register/producer', { name, cpf, email, password, genrePreferences });
}

/**
 * POST /register/listener  — Cadastrar OUVINTE (público)
 * Body: { name, cpf, email, password, favoriteGenres: ["funk","sertanejo"] }
 * Role atribuída automaticamente como "lover"
 */
export async function registerListener({ name, cpf, email, password, favoriteGenres }) {
  return api.post('/register/listener', { name, cpf, email, password, favoriteGenres });
}


// ============================================================
// 4. PERFIL — profile.js
// ============================================================

/**
 * POST /create  — Criar perfil com foto
 * FormData: profileData (JSON), profilePicture (File), coverPhoto (File, opcional)
 * Requer JWT
 */
export async function createProfile({ nickName, bio, city, profilePicture, coverPhoto }) {
  const form = new FormData();
  form.append('profileData', new Blob(
    [JSON.stringify({ nickName, bio, city })],
    { type: 'application/json' }
  ));
  form.append('profilePicture', profilePicture);
  if (coverPhoto) form.append('coverPhoto', coverPhoto);
  return api.post('/create', form, { headers: { 'Content-Type': 'multipart/form-data' } });
}

/**
 * GET /profile  — Ver meu perfil
 * Retorna: { urlProfile, nickName, coverPhotoUrl, bio, city }
 * Requer JWT
 */
export async function getMyProfile() {
  const res = await api.get('/profile');
  return res.data;
}


// ============================================================
// 5. FEED — feed.js
// ============================================================

/**
 * GET /feed?page=0&size=20  — Feed personalizado com scoring
 * Requer JWT. Retorna posts ordenados por relevância.
 * Produtor/Ouvinte: vê apenas posts de músicos.
 * Músico: vê feed de outros músicos.
 *
 * Retorna: FeedPage {
 *   content: TweetDetailResponse[],
 *   page, size, totalElements, totalPages, last
 * }
 */
export async function getPersonalizedFeed(page = 0, size = 20) {
  const res = await api.get('/feed', { params: { page, size } });
  return res.data;
}

/**
 * GET /feed/public?page=0&size=20  — Feed público sem autenticação
 */
export async function getPublicFeed(page = 0, size = 20) {
  const res = await api.get('/feed/public', { params: { page, size } });
  return res.data;
}


// ============================================================
// 6. POSTS (TWEETS) — posts.js
// ============================================================

/**
 * POST /tweet/create  — Criar post (somente MÚSICO)
 * FormData: tweetData (JSON), media (File, opcional)
 * Requer JWT
 */
export async function createPost({ title, messageContent, media }) {
  const form = new FormData();
  form.append('tweetData', new Blob(
    [JSON.stringify({ title, messageContent })],
    { type: 'application/json' }
  ));
  if (media) form.append('media', media);
  return api.post('/tweet/create', form, { headers: { 'Content-Type': 'multipart/form-data' } });
}

/**
 * GET /tweet/alltweets  — Feed geral (público)
 * Retorna: ResponseAllTweets[] (sem paginação, mais simples)
 */
export async function getAllTweets() {
  const res = await api.get('/tweet/alltweets');
  return res.data;
}

/**
 * GET /tweet/{id}  — Detalhe do post com likes e comentários
 * Requer JWT
 * Retorna: TweetDetailResponse {
 *   id, title, messageContent, mediaUrl, creationTimestamp,
 *   creator: { userId, name, nickname, urlImage },
 *   likesCount, likedByMe, comments: CommentResponse[]
 * }
 */
export async function getTweetById(id) {
  const res = await api.get(`/tweet/${id}`);
  return res.data;
}

/**
 * GET /tweet/user/{userId}  — Posts de um usuário específico
 * Requer JWT
 */
export async function getTweetsByUser(userId) {
  const res = await api.get(`/tweet/user/${userId}`);
  return res.data;
}

/**
 * DELETE /tweet/{id}  — Deletar post (somente dono)
 * Requer JWT
 */
export async function deleteTweet(id) {
  return api.delete(`/tweet/${id}`);
}

/**
 * POST /tweet/{id}/like  — Curtir / descurtir (toggle)
 * Requer JWT
 * Retorna: { likes: number }
 */
export async function toggleLike(tweetId) {
  const res = await api.post(`/tweet/${tweetId}/like`);
  return res.data;
}


// ============================================================
// 7. COMENTÁRIOS — comments.js
// ============================================================

/**
 * POST /tweet/{id}/comment  — Comentar em um post
 * Body: { text: "meu comentário" }
 * Requer JWT
 * Retorna: CommentResponse { id, text, creationTimestamp, author }
 */
export async function addComment(tweetId, text) {
  const res = await api.post(`/tweet/${tweetId}/comment`, { text });
  return res.data;
}

/**
 * GET /tweet/{id}/comments  — Listar comentários de um post
 * Requer JWT
 */
export async function getComments(tweetId) {
  const res = await api.get(`/tweet/${tweetId}/comments`);
  return res.data;
}

/**
 * DELETE /tweet/comment/{id}  — Deletar comentário (somente dono)
 * Requer JWT
 */
export async function deleteComment(commentId) {
  return api.delete(`/tweet/comment/${commentId}`);
}


// ============================================================
// 8. SEGUIDORES — follow.js
// ============================================================

/**
 * POST /follow/{userId}  — Seguir / deixar de seguir (toggle)
 * Requer JWT
 * Retorna: FollowResponse { followers, following, isFollowing }
 */
export async function toggleFollow(userId) {
  const res = await api.post(`/follow/${userId}`);
  return res.data;
}

/**
 * GET /follow/{userId}/stats  — Contagem de seguidores + se você segue
 * Requer JWT
 */
export async function getFollowStats(userId) {
  const res = await api.get(`/follow/${userId}/stats`);
  return res.data;
}

/**
 * GET /follow/{userId}/followers  — Lista de seguidores
 * Requer JWT
 */
export async function getFollowers(userId) {
  const res = await api.get(`/follow/${userId}/followers`);
  return res.data;
}

/**
 * GET /follow/{userId}/following  — Lista de quem o usuário segue
 * Requer JWT
 */
export async function getFollowing(userId) {
  const res = await api.get(`/follow/${userId}/following`);
  return res.data;
}


// ============================================================
// 9. CHAT (MENSAGENS DIRETAS) — chat.js
// ============================================================

/**
 * POST /chat/send  — Enviar mensagem direta
 * Body: { receiverId: "uuid", content: "texto" }
 * Requer JWT
 * Retorna: ChatMessageResponse
 */
export async function sendMessage(receiverId, content) {
  const res = await api.post('/chat/send', { receiverId, content });
  return res.data;
}

/**
 * GET /chat/conversations  — Inbox (prévia de todas as conversas)
 * Requer JWT
 * Retorna: ConversationPreview[] { partner, lastMessage, lastMessageAt, unreadCount }
 */
export async function getConversationList() {
  const res = await api.get('/chat/conversations');
  return res.data;
}

/**
 * GET /chat/conversation/{partnerId}  — Conversa completa com um usuário
 * Requer JWT. Marca mensagens como lidas automaticamente.
 * Retorna: ChatMessageResponse[]
 */
export async function getConversation(partnerId) {
  const res = await api.get(`/chat/conversation/${partnerId}`);
  return res.data;
}


// ============================================================
// 10. NOTIFICAÇÕES — notifications.js
// ============================================================

/**
 * GET /notifications  — Minhas notificações
 * Requer JWT
 * Retorna: NotificationResponse[] { id, type, message, actor, read, createdAt }
 * Types: LIKE | COMMENT | FOLLOW | MENTION
 */
export async function getNotifications() {
  const res = await api.get('/notifications');
  return res.data;
}

/**
 * GET /notifications/unread/count  — Badge de não lidas
 * Requer JWT
 * Retorna: { unread: number }
 */
export async function getUnreadCount() {
  const res = await api.get('/notifications/unread/count');
  return res.data.unread;
}

/**
 * PATCH /notifications/read-all  — Marcar todas como lidas
 * Requer JWT
 */
export async function markAllNotificationsRead() {
  return api.patch('/notifications/read-all');
}

/**
 * PATCH /notifications/{id}/read  — Marcar uma como lida
 * Requer JWT
 */
export async function markNotificationRead(id) {
  return api.patch(`/notifications/${id}/read`);
}


// ============================================================
// 11. EVENTOS — events.js
// ============================================================

/**
 * POST /events  — Criar evento (somente PRODUTOR)
 * Body: {
 *   name: "Festival Samba SP",
 *   description: "Grande festival...",
 *   location: "Parque Ibirapuera, São Paulo",
 *   dateTime: "2025-09-15T20:00:00Z",
 *   musicalGenres: ["samba", "pagode"]
 * }
 * Requer JWT (role: producer)
 * Retorna: EventResponse
 */
export async function createEvent(data) {
  const res = await api.post('/events', data);
  return res.data;
}

/**
 * PUT /events/{id}  — Atualizar evento (somente dono)
 * Body: mesmo formato do createEvent
 * Requer JWT (role: producer, dono do evento)
 */
export async function updateEvent(id, data) {
  const res = await api.put(`/events/${id}`, data);
  return res.data;
}

/**
 * DELETE /events/{id}  — Deletar evento (somente dono)
 * Requer JWT
 */
export async function deleteEvent(id) {
  return api.delete(`/events/${id}`);
}

/**
 * GET /events?page=0&size=20  — Listar eventos futuros (público)
 * Retorna: EventResponse[] {
 *   id, name, description, location, dateTime, createdAt,
 *   producer, musicalGenres, performers, attendeesCount
 * }
 */
export async function getUpcomingEvents(page = 0, size = 20) {
  const res = await api.get('/events', { params: { page, size } });
  return res.data;
}

/**
 * GET /events/{id}  — Detalhe de evento (público)
 */
export async function getEventById(id) {
  const res = await api.get(`/events/${id}`);
  return res.data;
}

/**
 * GET /events/genre/{genre}  — Eventos por gênero (público)
 * Exemplo: GET /events/genre/samba
 */
export async function getEventsByGenre(genre) {
  const res = await api.get(`/events/genre/${genre}`);
  return res.data;
}

/**
 * GET /events/me  — Meus eventos
 * Produtor: eventos que criou
 * Músico: eventos em que é performer
 * Requer JWT
 */
export async function getMyEvents() {
  const res = await api.get('/events/me');
  return res.data;
}

/**
 * POST /events/{id}/rsvp  — Confirmar / cancelar presença (toggle)
 * Requer JWT (qualquer role)
 */
export async function rsvpEvent(id) {
  const res = await api.post(`/events/${id}/rsvp`);
  return res.data;
}


// ============================================================
// 12. PROPOSTAS — proposals.js
// ============================================================

/**
 * POST /events/{eventId}/proposals  — Produtor envia proposta para músico
 * Body: { musicianId: "uuid", message: "Olá músico, gostaria de convidá-lo..." }
 * Requer JWT (role: producer, dono do evento)
 * Retorna: ProposalResponse {
 *   id, event, producer, musician, message, responseMessage, status, createdAt, respondedAt
 * }
 * Status: PENDING | ACCEPTED | DECLINED | CANCELLED
 */
export async function sendProposal(eventId, musicianId, message) {
  const res = await api.post(`/events/${eventId}/proposals`, { musicianId, message });
  return res.data;
}

/**
 * GET /proposals/received  — Músico vê propostas recebidas
 * Requer JWT (role: musician)
 */
export async function getReceivedProposals() {
  const res = await api.get('/proposals/received');
  return res.data;
}

/**
 * GET /proposals/sent  — Produtor vê propostas enviadas
 * Requer JWT (role: producer)
 */
export async function getSentProposals() {
  const res = await api.get('/proposals/sent');
  return res.data;
}

/**
 * GET /proposals/pending/count  — Badge de propostas pendentes (músico)
 * Requer JWT
 * Retorna: { pending: number }
 */
export async function getPendingProposalsCount() {
  const res = await api.get('/proposals/pending/count');
  return res.data.pending;
}

/**
 * PATCH /proposals/{id}/answer  — Músico responde proposta
 * Body: { accepted: true, responseMessage: "Topei! Estarei lá." }
 * Body: { accepted: false, responseMessage: "Infelizmente não posso." }
 * Requer JWT (role: musician, destinatário da proposta)
 * Ao aceitar: músico é adicionado como performer no evento automaticamente
 */
export async function answerProposal(proposalId, accepted, responseMessage = '') {
  const res = await api.patch(`/proposals/${proposalId}/answer`, { accepted, responseMessage });
  return res.data;
}

/**
 * DELETE /proposals/{id}  — Produtor cancela proposta pendente
 * Requer JWT (role: producer, dono da proposta)
 */
export async function cancelProposal(proposalId) {
  const res = await api.delete(`/proposals/${proposalId}`);
  return res.data;
}


// ============================================================
// 13. MÚSICOS (DISCOVERY) — musicians.js
// ============================================================

/**
 * GET /musicians  — Lista todos os músicos (para produtores)
 * Requer JWT
 * Retorna: UserProfileResponse[] {
 *   userId, name, email, role, musicalGenres, nickname, urlProfile, city, bio
 * }
 */
export async function getAllMusicians() {
  const res = await api.get('/musicians');
  return res.data;
}

/**
 * GET /musicians/genre/{genre}  — Músicos por gênero
 * Requer JWT
 * Exemplo: GET /musicians/genre/samba
 */
export async function getMusiciansByGenre(genre) {
  const res = await api.get(`/musicians/genre/${genre}`);
  return res.data;
}

/**
 * GET /musicians/{id}  — Perfil de um músico (público)
 */
export async function getMusicianProfile(id) {
  const res = await api.get(`/musicians/${id}`);
  return res.data;
}


// ============================================================
// 14. RANKING DE GÊNEROS — ranking.js
// ============================================================

/**
 * GET /ranking/genres  — Ranking de gêneros da plataforma (público)
 * Retorna: GenreRankingResponse[] {
 *   position, genre, totalLikes, totalComments, totalPosts, totalMusicians,
 *   rankingScore, lastUpdated
 * }
 * Fórmula: score = likes×1.0 + comentários×2.0 + posts×0.5 + músicos×3.0
 */
export async function getGenreRanking() {
  const res = await api.get('/ranking/genres');
  return res.data;
}

/**
 * POST /ranking/genres/refresh  — Recalcula o ranking
 * Requer JWT (qualquer usuário autenticado)
 * Use com moderação — operação pesada no banco
 */
export async function refreshGenreRanking() {
  const res = await api.post('/ranking/genres/refresh');
  return res.data;
}


// ============================================================
// 15. ESTRUTURA DE PÁGINAS RECOMENDADA
// ============================================================
/**
 *
 * /login                  → Tela de login
 * /register               → Escolha: Músico | Produtor | Ouvinte
 * /register/musician      → Formulário de músico
 * /register/producer      → Formulário de produtor com preferências de gênero
 * /register/listener      → Formulário de ouvinte
 *
 * /                       → Feed personalizado (requer login)
 * /explore                → Feed público + ranking de gêneros (sem login)
 *
 * /post/create            → Criar post (somente músico)
 * /post/:id               → Detalhe do post
 *
 * /events                 → Lista de eventos futuros (público)
 * /events/:id             → Detalhe de evento
 * /events/create          → Criar evento (somente produtor)
 * /events/me              → Meus eventos
 *
 * /proposals/received     → Propostas recebidas (músico)
 * /proposals/sent         → Propostas enviadas (produtor)
 *
 * /musicians              → Descobrir músicos (produtor)
 * /musicians/:id          → Perfil público de músico
 *
 * /chat                   → Inbox
 * /chat/:partnerId        → Conversa
 *
 * /notifications          → Notificações
 * /profile                → Meu perfil
 * /profile/:userId        → Perfil de outro usuário
 *
 * /ranking                → Ranking de gêneros
 *
 * ============================================================
 * REGRAS DE UI POR ROLE
 * ============================================================
 *
 * MÚSICO (musician):
 *   - Pode criar posts, curtir, comentar
 *   - Vê feed de músicos com scoring por gênero
 *   - Recebe propostas de produtores → aceita ou recusa
 *   - Menu extra: "Minhas Propostas" com badge de pendentes
 *   - NÃO pode criar eventos
 *
 * PRODUTOR (producer):
 *   - PODE criar, editar, deletar eventos
 *   - Pode enviar propostas para músicos
 *   - Pode descobrir músicos por gênero (/musicians)
 *   - Vê feed de músicos com scoring por suas preferências de gênero
 *   - Menu extra: "Meus Eventos", "Propostas Enviadas"
 *   - NÃO pode criar posts musicais
 *
 * OUVINTE (lover):
 *   - Somente consome: vê feed, eventos, ranking
 *   - Pode curtir, comentar, seguir
 *   - Pode confirmar presença em eventos (RSVP)
 *   - NÃO pode criar posts, NÃO pode criar eventos, NÃO pode enviar propostas
 *   - Feed personalizado com scoring por seus gêneros favoritos
 *
 * ============================================================
 * GÊNEROS DISPONÍVEIS (valores exatos para enviar na API)
 * ============================================================
 * "samba" | "pagode" | "forro" | "sertanejo" | "funk"
 */
