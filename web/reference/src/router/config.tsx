import { lazy } from 'react';
import { RouteObject } from 'react-router-dom';

const Home = lazy(() => import('../pages/home/page'));
const Login = lazy(() => import('../pages/login/page'));
const Community = lazy(() => import('../pages/community/page'));
const Live = lazy(() => import('../pages/live/page'));
const LiveDetail = lazy(() => import('../pages/live-detail/page'));
const Voting = lazy(() => import('../pages/voting/page'));
const Ads = lazy(() => import('../pages/ads/page'));
const Membership = lazy(() => import('../pages/membership/page'));
const Chart = lazy(() => import('../pages/chart/page'));
const Concert = lazy(() => import('../pages/concert/page'));
const MyPage = lazy(() => import('../pages/mypage/page'));
const NewsDetail = lazy(() => import('../pages/news-detail/page'));
const PostDetail = lazy(() => import('../pages/post-detail/page'));
const PostCreate = lazy(() => import('../pages/post-create/page'));
const ArtistDetail = lazy(() => import('../pages/artist-detail/page'));
const ConcertDetail = lazy(() => import('../pages/concert-detail/page'));
const Settings = lazy(() => import('../pages/settings/page'));
const Notifications = lazy(() => import('../pages/notifications/page'));
const Search = lazy(() => import('../pages/search/page'));
const Favorites = lazy(() => import('../pages/favorites/page'));
const Saved = lazy(() => import('../pages/saved/page'));
const Tickets = lazy(() => import('../pages/tickets/page'));
const Support = lazy(() => import('../pages/support/page'));
const ErrorPage = lazy(() => import('../pages/error/page'));
const NotFound = lazy(() => import('../pages/NotFound'));
const NoticeDetail = lazy(() => import("../pages/notice-detail/page"));

const routes: RouteObject[] = [
  {
    path: '/',
    element: <Home />,
  },
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/community',
    element: <Community />,
  },
  {
    path: '/live',
    element: <Live />,
  },
  {
    path: '/live-detail',
    element: <LiveDetail />,
  },
  {
    path: '/voting',
    element: <Voting />,
  },
  {
    path: '/ads',
    element: <Ads />,
  },
  {
    path: '/membership',
    element: <Membership />,
  },
  {
    path: '/chart',
    element: <Chart />,
  },
  {
    path: '/concert',
    element: <Concert />,
  },
  {
    path: '/mypage',
    element: <MyPage />,
  },
  {
    path: '/news-detail',
    element: <NewsDetail />,
  },
  {
    path: '/post-detail',
    element: <PostDetail />,
  },
  {
    path: '/post-create',
    element: <PostCreate />,
  },
  {
    path: '/artist-detail',
    element: <ArtistDetail />,
  },
  {
    path: '/concert-detail',
    element: <ConcertDetail />,
  },
  {
    path: '/settings',
    element: <Settings />,
  },
  {
    path: '/notifications',
    element: <Notifications />,
  },
  {
    path: '/search',
    element: <Search />,
  },
  {
    path: '/favorites',
    element: <Favorites />,
  },
  {
    path: '/saved',
    element: <Saved />,
  },
  {
    path: '/tickets',
    element: <Tickets />,
  },
  {
    path: '/support',
    element: <Support />,
  },
  {
    path: '/error',
    element: <ErrorPage />,
  },
  {
    path: '/notice-detail',
    element: <NoticeDetail />,
  },
  {
    path: '*',
    element: <NotFound />,
  },
];

export default routes;
