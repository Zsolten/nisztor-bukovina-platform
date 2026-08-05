export const resources = {
  hu: {
    translation: {
      app: {
        title: 'Nisztor–Bukovina Panziók',
        location: 'Csernakeresztúr · Dél-Erdély',
        navigation: {
          guesthouses: 'Panzióink',
          home: 'Főoldal',
          languages: 'Nyelvválasztás',
        },
        footer: {
          message: 'Családias vendéglátás Csernakeresztúron.',
          copyright: 'Nisztor–Bukovina Panziók',
        },
      },
      guesthouses: {
        eyebrow: 'Két panzió · egy vendégszerető család',
        title: 'Szeretettel köszöntjük honlapunkon!',
        introduction:
          'Csendes, nyugodt, családias környezetben igényes szálláslehetőséget kínálunk minden betérő vendégünknek.',
        roomCount: '{{count}} szoba',
        openDetails: 'Megnézem a panziót',
        sectionTitle: 'Válassza ki az otthonát Csernakeresztúron',
        sectionIntroduction:
          'Mindkét panzióban tiszta, rendezett szobák és kedves, udvarias kiszolgálás várja vendégeinket.',
        loading: 'A panziók betöltése…',
        loadError: 'A panziók most nem tölthetők be. Kérjük, próbálja újra később.',
        back: 'Vissza a panziókhoz',
        rooms: 'Szobáink',
        gallery: 'Képgaléria',
        galleryHint: 'A nagyításhoz válasszon egy képet.',
        closeGallery: 'Kép bezárása',
        previousImage: 'Előző kép',
        nextImage: 'Következő kép',
        imageCounter: '{{current}} / {{total}}',
        detailError: 'A keresett panzió nem található vagy jelenleg nem érhető el.',
      },
    },
  },
  ro: {
    translation: {
      app: {
        title: 'Pensiunile Nisztor–Bukovina',
        location: 'Cristur · Transilvania de Sud',
        navigation: {
          guesthouses: 'Pensiunile noastre',
          home: 'Acasă',
          languages: 'Selectarea limbii',
        },
        footer: {
          message: 'Ospitalitate familială în Cristur.',
          copyright: 'Pensiunile Nisztor–Bukovina',
        },
      },
      guesthouses: {
        eyebrow: 'Două pensiuni · o familie ospitalieră',
        title: 'Bine ați venit pe pagina noastră!',
        introduction: 'Oferim cazare de calitate într-un mediu liniștit, relaxant și familial.',
        roomCount: '{{count}} camere',
        openDetails: 'Descoperă pensiunea',
        sectionTitle: 'Alegeți-vă casa din Cristur',
        sectionIntroduction:
          'În ambele pensiuni vă așteaptă camere curate și confortabile, precum și servicii amabile.',
        loading: 'Se încarcă pensiunile…',
        loadError: 'Pensiunile nu pot fi încărcate. Vă rugăm să încercați din nou mai târziu.',
        back: 'Înapoi la pensiuni',
        rooms: 'Camerele noastre',
        gallery: 'Galerie foto',
        galleryHint: 'Selectați o imagine pentru a o mări.',
        closeGallery: 'Închide imaginea',
        previousImage: 'Imaginea anterioară',
        nextImage: 'Imaginea următoare',
        imageCounter: '{{current}} / {{total}}',
        detailError: 'Pensiunea nu există sau nu este disponibilă momentan.',
      },
    },
  },
  en: {
    translation: {
      app: {
        title: 'Nisztor–Bukovina Guesthouses',
        location: 'Cristur · Southern Transylvania',
        navigation: {
          guesthouses: 'Our guesthouses',
          home: 'Home',
          languages: 'Language selection',
        },
        footer: {
          message: 'Family hospitality in Cristur.',
          copyright: 'Nisztor–Bukovina Guesthouses',
        },
      },
      guesthouses: {
        eyebrow: 'Two guesthouses · one welcoming family',
        title: 'A warm welcome to our home!',
        introduction:
          'We offer quality accommodation in a quiet, peaceful and welcoming family environment.',
        roomCount: '{{count}} rooms',
        openDetails: 'Discover the guesthouse',
        sectionTitle: 'Choose your home in Cristur',
        sectionIntroduction:
          'Both guesthouses offer clean, comfortable rooms and warm, attentive hospitality.',
        loading: 'Loading guesthouses…',
        loadError: 'The guesthouses cannot be loaded right now. Please try again later.',
        back: 'Back to guesthouses',
        rooms: 'Our rooms',
        gallery: 'Gallery',
        galleryHint: 'Select an image to enlarge it.',
        closeGallery: 'Close image',
        previousImage: 'Previous image',
        nextImage: 'Next image',
        imageCounter: '{{current}} / {{total}}',
        detailError: 'This guesthouse does not exist or is currently unavailable.',
      },
    },
  },
} as const

export type TranslationResources = (typeof resources)['hu']['translation']
