"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import { motion } from "framer-motion";
import Link from "next/link";

export default function LivePage() {
  const liveStreams = [
    {
      id: 1,
      title: 'NewJeans 컴백 쇼케이스',
      artist: 'NewJeans Official',
      viewers: '24.5K',
      thumbnail: 'https://readdy.ai/api/search-image?query=K-pop%20girl%20group%20NewJeans%20performing%20live%20on%20stage%20with%20dynamic%20lighting%2C%20professional%20concert%20photography%2C%20energetic%20performance%2C%20vibrant%20stage%20lights%2C%20multiple%20members%20singing%20and%20dancing%2C%20high-quality%20broadcast%20camera%20angle%2C%20modern%20stage%20design%2C%20purple%20and%20pink%20lighting%20effects%2C%20professional%20live%20streaming%20quality%2C%204K%20resolution%2C%20cinematic%20composition&width=343&height=193&seq=live001&orientation=landscape',
      isLive: true,
      duration: null
    },
    {
      id: 2,
      title: 'BTS Fan Meeting Special',
      artist: 'BTS',
      viewers: '89.2K',
      thumbnail: 'https://readdy.ai/api/search-image?query=KPOP%20fan%20meeting%20event%2C%20intimate%20stage%20setup%2C%20warm%20purple%20lighting%2C%20cozy%20atmosphere%2C%20professional%20event%20photography%2C%20fans%20interaction%20space&width=600&height=400&seq=live102&orientation=landscape',
      duration: '1:45:20',
      isLive: false
    },
    {
      id: 3,
      title: 'BLACKPINK Behind The Scenes',
      artist: 'BLACKPINK',
      viewers: '67.8K',
      thumbnail: 'https://readdy.ai/api/search-image?query=KPOP%20behind%20the%20scenes%20photoshoot%2C%20backstage%20atmosphere%2C%20pink%20and%20black%20theme%2C%20professional%20photography%2C%20glamorous%20setting%2C%20makeup%20room&width=600&height=400&seq=live103&orientation=landscape',
      duration: '0:58:12',
      isLive: false
    },
    {
      id: 4,
      title: 'SEVENTEEN Dance Practice',
      artist: 'SEVENTEEN',
      viewers: '45.1K',
      thumbnail: 'https://readdy.ai/api/search-image?query=KPOP%20boy%20group%20dance%20practice%20room%2C%20wide%20mirror%20reflection%2C%20casual%20training%20outfits%2C%20synchronized%20choreography%2C%20bright%20studio%20lighting&width=600&height=400&seq=live104&orientation=landscape',
      duration: '0:24:15',
      isLive: false
    },
    {
      id: 5,
      title: 'Stray Kids World Tour Highlights',
      artist: 'Stray Kids',
      viewers: '38.9K',
      thumbnail: 'https://readdy.ai/api/search-image?query=KPOP%20concert%20highlight%20moment%2C%20fireworks%20on%20stage%2C%20energetic%20performance%2C%20massive%20crowd%20cheering%2C%20stadium%20atmosphere&width=600&height=400&seq=live105&orientation=landscape',
      duration: '2:10:05',
      isLive: false
    },
    {
      id: 6,
      title: 'TWICE TV Show Appearance',
      artist: 'TWICE',
      viewers: '52.3K',
      thumbnail: 'https://readdy.ai/api/search-image?query=KPOP%20idol%20TV%20show%20talk%20show%20set%2C%20bright%20studio%20lighting%2C%20colorful%20background%2C%20professional%20broadcast%20camera%20angle&width=600&height=400&seq=live106&orientation=landscape',
      duration: '0:45:30',
      isLive: false
    }
  ];

  const container = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const item = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0 }
  };

  return (
    <>
      <PageHeader title="Live Now" />
      <PageWrapper>
        <div className="max-w-7xl mx-auto px-4 lg:px-8 py-6">
          <motion.div 
            variants={container}
            initial="hidden"
            animate="show"
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
          >
            {liveStreams.map((stream) => (
              <motion.div key={stream.id} variants={item}>
                <Link href="/live-detail" className="block bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow group">
                  <div className="relative">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img
                      src={stream.thumbnail}
                      alt={stream.title}
                      className="w-full h-48 object-cover object-top group-hover:scale-105 transition-transform duration-300"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
                    
                    {stream.isLive ? (
                      <div className="absolute top-3 left-3 bg-red-600 text-white text-xs px-2.5 py-1 rounded-full font-medium flex items-center gap-1.5 animate-pulse">
                        <span className="w-1.5 h-1.5 bg-white rounded-full"></span>
                        LIVE
                      </div>
                    ) : (
                      <div className="absolute bottom-3 right-3 bg-black/70 text-white text-xs px-2 py-1 rounded-md font-medium">
                        {stream.duration}
                      </div>
                    )}
                  </div>
                  <div className="p-4">
                    <h3 className="font-bold text-gray-900 text-lg line-clamp-1 group-hover:text-purple-600 transition-colors">
                      {stream.title}
                    </h3>
                    <p className="text-sm text-gray-500 mt-1">{stream.artist}</p>
                    <div className="flex items-center gap-4 mt-3 text-xs text-gray-400">
                      <span className="flex items-center gap-1">
                        <i className="ri-eye-line"></i> {stream.viewers}
                      </span>
                      <span className="flex items-center gap-1">
                        <i className="ri-thumb-up-line"></i> 1.2K
                      </span>
                    </div>
                  </div>
                </Link>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </PageWrapper>
    </>
  );
}