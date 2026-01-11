"use client";

import PageHeader from "@/components/layout/PageHeader";
import PageWrapper from "@/components/layout/PageWrapper";
import Link from "next/link";

export default function ConcertPage() {
  const concerts = [
    {
      id: 1,
      title: 'BTS World Tour Seoul',
      artist: 'BTS',
      date: 'Dec 20, 2024',
      time: '19:00 KST',
      venue: 'Jamsil Olympic Stadium',
      location: 'Seoul, Korea',
      price: '₩150,000 - ₩300,000',
      status: 'available',
      image: 'https://readdy.ai/api/search-image?query=BTS%20concert%20poster%20design%2C%20purple%20theme%2C%20professional%20concert%20photography%2C%20stadium%20stage%20setup%2C%20massive%20LED%20screens%2C%20spectacular%20lighting%2C%20energetic%20atmosphere&width=600&height=400&seq=concert001&orientation=landscape'
    },
    {
      id: 2,
      title: 'BLACKPINK World Tour',
      artist: 'BLACKPINK',
      date: 'Dec 25, 2024',
      time: '18:00 KST',
      venue: 'KSPO Dome',
      location: 'Seoul, Korea',
      price: '₩180,000 - ₩350,000',
      status: 'available',
      image: 'https://readdy.ai/api/search-image?query=BLACKPINK%20concert%20poster%20design%2C%20pink%20and%20black%20theme%2C%20professional%20concert%20photography%2C%20glamorous%20stage%20setup%2C%20powerful%20lighting%2C%20fierce%20atmosphere&width=600&height=400&seq=concert002&orientation=landscape'
    },
    {
      id: 3,
      title: 'SEVENTEEN Be The Sun',
      artist: 'SEVENTEEN',
      date: 'Jan 5, 2025',
      time: '19:00 KST',
      venue: 'Gocheok Sky Dome',
      location: 'Seoul, Korea',
      price: '₩140,000 - ₩280,000',
      status: 'available',
      image: 'https://readdy.ai/api/search-image?query=SEVENTEEN%20concert%20poster%20design%2C%20bright%20colorful%20theme%2C%20professional%20concert%20photography%2C%20synchronized%20performance%2C%20vibrant%20stage%20lighting%2C%20energetic%20vibe&width=600&height=400&seq=concert003&orientation=landscape'
    },
    {
      id: 4,
      title: 'NewJeans Fan Meeting',
      artist: 'NewJeans',
      date: 'Jan 10, 2025',
      time: '17:00 KST',
      venue: 'Olympic Hall',
      location: 'Seoul, Korea',
      price: '₩120,000 - ₩250,000',
      status: 'soldout',
      image: 'https://readdy.ai/api/search-image?query=NewJeans%20fan%20meeting%20poster%20design%2C%20fresh%20pastel%20theme%2C%20professional%20event%20photography%2C%20intimate%20stage%20setup%2C%20youthful%20aesthetic%2C%20trendy%20atmosphere&width=600&height=400&seq=concert004&orientation=landscape'
    },
    {
      id: 5,
      title: 'TWICE Encore Concert',
      artist: 'TWICE',
      date: 'Jan 18, 2025',
      time: '19:00 KST',
      venue: 'KSPO Dome',
      location: 'Seoul, Korea',
      price: '₩160,000 - ₩320,000',
      status: 'available',
      image: 'https://readdy.ai/api/search-image?query=TWICE%20concert%20poster%20design%2C%20colorful%20vibrant%20theme%2C%20professional%20concert%20photography%2C%20energetic%20stage%20setup%2C%20spectacular%20lighting%20effects%2C%20joyful%20atmosphere&width=600&height=400&seq=concert005&orientation=landscape'
    }
  ];

  return (
    <>
      <PageHeader title="Upcoming Concerts" />
      <PageWrapper>
        <div className="max-w-7xl mx-auto px-4 lg:px-8 py-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {concerts.map(concert => (
              <div key={concert.id} className="bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src={concert.image}
                  alt={concert.title}
                  className="w-full h-48 object-cover object-top"
                />
                <div className="p-4">
                  <h3 className="font-bold text-gray-900 text-lg">{concert.title}</h3>
                  <p className="text-sm text-gray-600 mt-1">{concert.artist}</p>
                  <div className="mt-3 space-y-2">
                    <div className="flex items-center text-sm text-gray-600">
                      <i className="ri-calendar-line w-5"></i>
                      <span>{concert.date} at {concert.time}</span>
                    </div>
                    <div className="flex items-center text-sm text-gray-600">
                      <i className="ri-map-pin-line w-5"></i>
                      <span>{concert.location}</span>
                    </div>
                    <div className="flex items-center text-sm text-gray-600">
                      <i className="ri-ticket-line w-5"></i>
                      <span>{concert.price}</span>
                    </div>
                  </div>
                  <Link
                    href={`/concert-detail?id=${concert.id}`}
                    className="w-full mt-4 bg-gradient-to-r from-purple-600 to-pink-600 text-white font-medium py-3 rounded-full block text-center hover:shadow-lg transition-shadow"
                  >
                    Get Tickets
                  </Link>
                </div>
              </div>
            ))}
          </div>
        </div>
      </PageWrapper>
    </>
  );
}